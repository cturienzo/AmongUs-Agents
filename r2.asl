// mars robot 2

/* Initial beliefs */

at(P) :- pos(P,X,Y) & pos(r2,X,Y).

/* Initial goal */

!check(slots).

/* Plans */
+!check(slots) : not tarea_completada(r2)
   <- next(slot);
      !check(slots).

+!check(slots): tarea_completada(r2)
	<- sabotear(tarea); next(slot);
		!check(slots).

