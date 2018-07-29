# ambient

Example of using ambient functions from self-hosted ClojureScript.

We have a `main.core` namespace which provides an `evaluate` function, and this example will show how to call functions in an ambient (pre-loaded) `library.core` namespace.

First, start up a browser REPL via

```
$ clj -m cljs.main
```

At the REPL, load our main namespace by evaluating

```clojure
(require 'main.core)
```

Test that we can evaluate some code:

```clojure
(main.core/evaluate "(+ 2 3)" prn)
```

This should print

```clojure
{:ns cljs.user, :value 5}
```

Now, since `main.core` required `library.core`, we can call functions in that namespace. Evaluating this at the REPL

```clojure
(library.core/my-inc 10)
```

will yield `11`.

Now, let's try to use this "ambient" function from self-hosted ClojureScript:

```clojure
(main.core/evaluate "(library.core/my-inc 10)" prn)
```

You will see the following

```
WARNING: No such namespace: library.core, could not locate library/core.cljs, library/core.cljc, or JavaScript source providing "library.core" at line 1
WARNING: Use of undeclared Var library.core/my-inc at line 1
{:ns cljs.user, :value 11}
```

In short, what is going on is that even though `library.core.my_inc` is available in the JavaScript environment, and can indeed be called, producing the correct answer, you get warnings from the self-hosted compiler that it knows nothing about this namespace.

This is because the compiler analysis metadata is not in the `main.core/state` atom.  (The self-hosted compiler has its own analysis state, held in that atom in the JavaScript environment, which is separate from the JVM compiler analysis state, held via Clojure in the Java environment.)

> Note: If we instead had the source for `library.core` compiled by the self-hosted compiler (by perhaps by using `main.core/evaluate` to eval `"(require 'library.core)"`, along with properly defining a `cljs.js/*load-fn*` that could retrieve this source, things would be good , and the compiler analysis metadata _would_ be in `main.core/state.` But this example is about calling ambient / pre-compiled functions in `library.core`.

We can fix this by making use of `cljs.js/load-analysis-cache!` to load the analysis cache associated with the `library.core` namespace.

This example code embeds this analysis cache directly in the code by employing a macro that snatches the analysis cache from the JVM-based compiler. You can transport this analysis cache to the browser by any mechanism you desire; this just illustrates one way of simply bundling it directly in the shipping code.

Go ahead and evaluate the following, just to see what the analysis cache for that namespace looks like:

```
(main.core/analyzer-state 'library.core)
```

If you call

```clojure
(main.core/load-library-analysis-cache!)
```

this analysis cache will be loaded for use by the self-hosted compiler.

Now if you evaluate

```clojure
(main.core/evaluate "(library.core/my-inc 10)" prn)
```

you won't see any warnings and

```clojure
{:ns cljs.user, :value 11}
```

will be printed.

Furthermore, since the self-hosted compiler now has the analysis metadata for `libraray.core`, it can properly warn on arity errors, for example

```clojure
(main.core/evaluate "(library.core/my-inc 10 12)" prn)
```

will cause 

```
WARNING: Wrong number of args (2) passed to library.core/my-inc at line 1
```

to be printed.

The above illustrates what happens when you don't have the analyzer cache present for a namespace and how to fix it using `cljs.js/load-analysis-cache!`. If you know that you will always want to load the cache upon startup, you can simply things, making use of an optional second argument to `cljs.js/empty-state` to load this cache at initialization time:

```clojure
(def state (cljs.js/empty-state
             (fn [state]
               (assoc-in state [:cljs.analyzer/namespaces 'library.core]
                 (analyzer-state 'library.core)))))
```    