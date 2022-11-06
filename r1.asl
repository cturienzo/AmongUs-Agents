// mars robot 1

/* Initial beliefs */

at(P) :- pos(P,X,Y) & pos(r1,X,Y).


/* Initial goal */

!check(slots).

/* Plans */

// desplazarse
+!check(slots) : not tarea(r1)
   <- next_crew(slot);
      !check(slots).

+!check(slots): tarea(r1)
	<- realizar_tarea(tarea);
		!check(slots).
		
+oxigeno_saboteado(pos_ox)
	<- !at(ox);
	   arreglar_oxigeno(oxigeno);
	   !check(slots).
	   

+reactor_saboteado(pos_re)
	<- !at(re);
	   arreglar_reactor(reactor);
	   !check(slots).
	   

+!at(L) : at(L).
+!at(L) <- ?pos(L,X,Y);
           move_towards(X,Y);
           !at(L).
		


/*
@lg[atomic]
+garbage(r1) : not .desire(carry_to(r2))
   <- !carry_to(r2).

+!carry_to(R)
   <- // remember where to go back
      ?pos(r1,X,Y);
      -+pos(last,X,Y);

      // carry garbage to r2
      !take(garb,R);

      // goes back and continue to check
      !at(last);
      !check(slots).

+!take(S,L) : true
   <- !ensure_pick(S);
      !at(L);
      drop(S).

+!ensure_pick(S) : garbage(r1)
   <- pick(garb);
      !ensure_pick(S).
+!ensure_pick(_).

+!at(L) : at(L).
+!at(L) <- ?pos(L,X,Y);
           move_towards(X,Y);
           !at(L).
*/
