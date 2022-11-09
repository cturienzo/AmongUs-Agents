package AmongUs;

import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Random;
import java.util.logging.Logger;

public class NaveEnv extends Environment {

	
    public static final Term ns_crew = Literal.parseLiteral("next_crew(slot)");
	public static final Term ns_imp = Literal.parseLiteral("next_imp(slot)");
	
	public static final Term mt = Literal.parseLiteral("moverse_a_tarea(tripulante)");
	public static final Term rt = Literal.parseLiteral("realizar_tarea(tarea)");
	
	public static final Term so = Literal.parseLiteral("sabotear_oxigeno(oxigeno)");
	public static final Term ao = Literal.parseLiteral("arreglar_oxigeno(oxigeno)");
	
	public static final Term sr = Literal.parseLiteral("sabotear_reactor(reactor)");
	public static final Term ar = Literal.parseLiteral("arreglar_reactor(reactor)");


    public static final Literal g1 = Literal.parseLiteral("tarea(tripulante)");
    public static final Literal g2 = Literal.parseLiteral("tarea_completada(tripulante)");
	
	public static final Literal int_sab_ox = Literal.parseLiteral("intencion_sabotear_ox(impostor)");
	public static final Literal int_sab_re = Literal.parseLiteral("intencion_sabotear_re(impostor)");


	public static final Literal ox_sab = Literal.parseLiteral("oxigeno_saboteado(pos_ox)");
	public static final Literal re_sab = Literal.parseLiteral("reactor_saboteado(pos_re)");


    static Logger logger = Logger.getLogger(NaveEnv.class.getName());

    private NaveModel model;
    private NaveView view;
	
	private int sleep;	
	
    @Override
    public void init(String[] args) {
		int num_tripulantes = Integer.parseInt(args[0]);
		int num_impostores = Integer.parseInt(args[1]);
		sleep = Integer.parseInt(args[2]);
		
        model = new NaveModel(num_tripulantes, num_impostores);
        view  = new NaveView(model);
        model.setView(view);

		Location ox_loc = model.getOxigenoLocation();
		Location react_loc = model.getReactorLocation();
        
		//Posicion del oxigeno y del reactor
		addPercept(Literal.parseLiteral("pos(ox," + ox_loc.x + "," + ox_loc.y + ")"));
		addPercept(Literal.parseLiteral("pos(re," + react_loc.x + "," + react_loc.y + ")"));
		
		updatePercepts();
    }

    @Override
    public boolean executeAction(String ag, Structure action) {
        logger.info(ag+" doing: "+ action);
		int agId = getAgIdBasedOnName(ag);

        try {
			Thread.sleep(sleep);
            if (action.equals(ns_crew)) {
                
				model.nextSlot_crewmate(agId);
			
			}else if(action.equals(ns_imp)){
			
				model.nextSlot_impostor(agId);
            
			} else if(action.equals(mt)) {
				
				model.asignar_nueva_tarea(agId);
				Location t = model.getTareaMasCercana(agId);
				model.moveTowards(agId, t.x, t.y);
				
			} else if (action.getFunctor().equals("move_towards")) {
                
				int x = (int)((NumberTerm)action.getTerm(0)).solve();
                	int y = (int)((NumberTerm)action.getTerm(1)).solve();
				model.moveTowards(agId, x,y);
				
            } else if (action.equals(rt)) {
				
                model.realizar_tarea(agId);      
            
			} else if (action.equals(so)) {
            
				model.sabotear_oxigeno(agId);
            
			}else if (action.equals(ao)) {
            
				model.arreglar_oxigeno(agId);
            
			}else if (action.equals(sr)) {
            
				model.sabotear_reactor(agId);
            
			}else if (action.equals(ar)) {
            
				model.arreglar_reactor(agId);
            
			}else {
            
				return false;
            
			}
        } catch (Exception e) {
            
			e.printStackTrace();
        
		}

        updatePercepts();
        informAgsEnvironmentChanged();
        return true;
    }
    
	private void updatePercepts() {
		updateAgsPercept();	
	}
	
	private void updateAgsPercept() {
        for (int id = 0; id < model.getNbOfAgs(); id++) {
			if (id < model.getNumTripulantes())
				updateTripulantePercepts(id);
			else
				updateImpostorPercepts(id);
        }
    }
	
	private void updateTripulantePercepts(int agId) {
		Location l = model.getAgPos(agId);
		String agName = "tripulante" + (agId+1);
		
		addPercept(agName, Literal.parseLiteral("pos(" + l.x + "," + l.y + ")"));
		
		if (model.hasObject(NaveModel.TAREA, l)) {
            addPercept(agName, Literal.parseLiteral("tarea(tripulante)"));
        }
		if (model.hasObject(NaveModel.TAREA_COMPLETADA, l)) {
			removePercept(agName, Literal.parseLiteral("tarea(tripulante)"));
        }
		if (!model.hasObject(NaveModel.TAREA, l)) {
            removePercept(agName, Literal.parseLiteral("tarea(tripulante)"));
        }

	}
	
	private void updateImpostorPercepts(int agId) {

		Location l = model.getAgPos(agId);
		int impostorId = agId - model.getNumTripulantes();
		String agName = "impostor" + (impostorId+1);
		
		addPercept(agName, Literal.parseLiteral("pos(" + l.x + "," + l.y + ")"));
		
	}
	
    /**
    void updateTripulantePercept() {
        clearPercepts();                                                
		
		Random random = new Random(System.currentTimeMillis());
		
        Location r1Loc = model.getAgPos(0);
		Location r2Loc = model.getAgPos(1);
		
		//Location nearest_Task = model.encontrarTarea();
		
        Literal pos1 = Literal.parseLiteral("pos(r1," + r1Loc.x + "," + r1Loc.y + ")");
		Literal pos2 = Literal.parseLiteral("pos(r2," + r2Loc.x + "," + r2Loc.y + ")");
		
		Location ox_loc = model.getOxigenoLocation();
		Location react_loc = model.getReactorLocation();

		Literal pos_ox = Literal.parseLiteral("pos(ox," + ox_loc.x + "," + ox_loc.y + ")");
		Literal pos_re = Literal.parseLiteral("pos(re," + react_loc.x + "," + react_loc.y + ")");
		//Literal nt_ant = Literal.parseLiteral("pos(nearest_task," + nearest_Task_ant.x  + "," + nearest_Task_ant.y + ")");

		// Percepciones de la posicion de cada jugador
        addPercept("tripulante", pos1);
		addPercept("impostor", pos2);
		
		/*
		Location tarea_cercana = model.tarea_mas_cercana;
		Literal nt = Literal.parseLiteral("pos(tarea_mas_cercana," + tarea_cercana.x  + "," + tarea_cercana.y + ")");
		
		logger.info("[tripulante] Yendo hacia la tarea en posicion: " + tarea_cercana);
				
		//addPercept("r1", nt);
		//addPercept("r1", nt_ant);
		
		if (!nt_ant.equals(nt)){
			addPercept("r1", nt);
			
			if (model.hasObject(TAREA_COMPLETADA, r1Loc)) {
				//removePercept("r1", nt_ant);
				nt_ant = nt;
				nearest_Task_ant.x = nearest_Task.x;
				nearest_Task_ant.y = nearest_Task.y;
			}
		}
		
		//Posicion del oxigeno y del reactor
		addPercept(pos_ox);
		addPercept(pos_re);

		
		// Percepciones de tareas
        if (model.hasObject(NaveModel.TAREA, r1Loc)) {
            addPercept("tripulante", Literal.parseLiteral("tarea(r1)"));
        }
		
		if (!model.hasObject(NaveModel.TAREA, r1Loc)) {
            removePercept("tripulante",Literal.parseLiteral("tarea(r1)"));
        }
		
		if (model.hasObject(NaveModel.TAREA_COMPLETADA, r1Loc)) {
			removePercept("tripulante",Literal.parseLiteral("tarea(r1)"));
        }
			
		
		boolean sabotajeDisponible = model.getSaboteoDisponible();	
		// logica sabotear
		if (sabotajeDisponible){
			
			int realizarSabotaje = random.nextInt(100); // probabilidad de realizar sabotaje
			if (realizarSabotaje >= 0 && realizarSabotaje < 5){
				 addPercept("impostor", Literal.parseLiteral("intencion_sabotear_ox(r2)"));
				 addPercept("tripulante",Literal.parseLiteral("oxigeno_saboteado(pos_ox)"));
				 model.setSaboteoDisponible(false);
				 model.setOxigenoSaboteado(true);
			} else if (realizarSabotaje >= 5 && realizarSabotaje < 10) {
				 addPercept("impostor", Literal.parseLiteral("intencion_sabotear_re(r2)"));
				 addPercept("tripulante",Literal.parseLiteral("reactor_saboteado(pos_re)"));
				 model.setSaboteoDisponible(false);
				 model.setReactorSaboteado(true);
			}
		}
		
		// logica arreglar oxigeno
		if (model.hasObject(NaveModel.OXIGENO, r1Loc) && model.getOxigenoSaboteado()){
            removePercept("tripulante",Literal.parseLiteral("oxigeno_saboteado(pos_ox)"));
			model.setSaboteoDisponible(true);
			model.setOxigenoSaboteado(false);
        }
		
		// logica arreglar reactor
		if (model.hasObject(NaveModel.REACTOR, r1Loc) && model.getReactorSaboteado()) {
            removePercept("tripulante",Literal.parseLiteral("reactor_saboteado(pos_re)"));
			model.setSaboteoDisponible(true);
			model.setReactorSaboteado(false);
        }
		
		
			logger.info("O2 " + model.getOxigenoSaboteado());
			logger.info("REACTOR "+ model.getReactorSaboteado());
    }
	*/

	private int getAgIdBasedOnName(String agName) {
		
		if (agName.startsWith("tripulante"))
			return (Integer.parseInt(agName.substring(agName.length()-1))) - 1;
		else 
			return model.getNumTripulantes();

	}
	
}
