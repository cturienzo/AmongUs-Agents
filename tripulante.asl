// tripulante

/* Initial beliefs */
at(P) :- pos(P,X,Y) & pos(tripulante,X,Y).


/* Initial goal */
!completar_tareas(tripulante).

/* Plans */

// hacer tarea
+!completar_tareas(tripulante): tarea(tripulante) & not oxigeno_saboteado(pos_ox) & not reactor_saboteado(pos_re)
    <- realizar_tarea(tarea);
       !completar_tareas(tripulante).

// desplazarse a la siguiente tarea más cercana
+!completar_tareas(tripulante): not tarea(tripulante) & not oxigeno_saboteado(pos_ox) & not reactor_saboteado(pos_re)
    <- moverse_a_tarea(tripulante);
       !completar_tareas(tripulante).

// arreglar oxigeno
+!completar_tareas(tripulante): oxigeno_saboteado(pos_ox)
    <- !at(ox);
       arreglar_oxigeno(oxigeno);
       !completar_tareas(tripulante).

// arreglar reactor
+!completar_tareas(tripulante): reactor_saboteado(pos_re)
    <- !at(re);
       arreglar_reactor(reactor);
       !completar_tareas(tripulante).
	   

//desplazamiento hacia el reactor o hacia el oxígeno
+!at(L) : at(L).

+!at(L): oxigeno_saboteado(pos_ox) | reactor_saboteado(pos_re)
	<- ?pos(L,X,Y);
       move_towards(X,Y);
       !at(L).

// si pierde la percepcion de los recursos saboteados, vuelve a hacer tareas
+!at(L): not oxigeno_saboteado(pos_ox) & not reactor_saboteado(pos_re)  
	<- !completar_tareas(tripulante).
