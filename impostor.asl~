// mars robot 2

/* Initial beliefs */

at(P) :- pos(P,X,Y) & pos(r2,X,Y).

/* Initial goal */

!check(slots).

/* Plans */
+!check(slots)<- next_imp(slot);
      !check(slots).

+intencion_sabotear_ox(r2)<- sabotear_oxigeno(oxigeno);
      !check(slots).
	  
+intencion_sabotear_re(r2)<- sabotear_reactor(reactor);
      !check(slots).



