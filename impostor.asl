// impostor

/* Initial beliefs */

at(P) :- pos(P,X,Y) & pos(impostor,X,Y).

/* Initial goal */

!check(slots).

/* Plans */
+!check(slots)<- next_imp(slot);
      !check(slots).

+intencion_sabotear_ox(impostor)<- sabotear_oxigeno(oxigeno);
      !check(slots).
	  
+intencion_sabotear_re(impostor)<- sabotear_reactor(reactor);
      !check(slots).



