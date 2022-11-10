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
	
	private Random random = new Random(System.currentTimeMillis());
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
		
		clearPercepts(agName);
		addPercept(agName, Literal.parseLiteral("pos(tripulante,"+ l.x + "," + l.y + ")"));
		
		// Percepciones de tareas
		if (model.hasObject(NaveModel.TAREA, l)) {
            addPercept(agName, Literal.parseLiteral("tarea(tripulante)"));
        }
		if (model.hasObject(NaveModel.TAREA_COMPLETADA, l)) {
			removePercept(agName, Literal.parseLiteral("tarea(tripulante)"));
        }
		if (!model.hasObject(NaveModel.TAREA, l)) {
            removePercept(agName, Literal.parseLiteral("tarea(tripulante)"));
        }

		// Percepciones de sabotajes
		if (model.getOxigenoSaboteado()){
			addPercept(agName,Literal.parseLiteral("oxigeno_saboteado(pos_ox)"));
		}else{
			removePercept(agName,Literal.parseLiteral("oxigeno_saboteado(pos_ox)"));
		}
		
		if (model.getReactorSaboteado()){
			addPercept(agName,Literal.parseLiteral("reactor_saboteado(pos_re)"));
		}else{
			removePercept(agName,Literal.parseLiteral("reactor_saboteado(pos_re)"));
		}
		
		if (model.hasObject(NaveModel.OXIGENO, l) && model.getOxigenoSaboteado()){
            //removePercept(agName,Literal.parseLiteral("oxigeno_saboteado(pos_ox)"));
			model.setSaboteoDisponible(true);
			model.setOxigenoSaboteado(false);
        }
		
		if (model.hasObject(NaveModel.REACTOR, l) && model.getReactorSaboteado()) {
            //removePercept(agName,Literal.parseLiteral("reactor_saboteado(pos_re)"));
			model.setSaboteoDisponible(true);
			model.setReactorSaboteado(false);
        }
	}
	
	private void updateImpostorPercepts(int agId) {

		Location l = model.getAgPos(agId);
		String agName = "impostor" ;
		
		addPercept(agName, Literal.parseLiteral("pos(impostor," + l.x + "," + l.y + ")"));
		
		Random random = new Random(System.currentTimeMillis());
		boolean sabotajeDisponible = model.getSaboteoDisponible();	
		// logica sabotear
		if (sabotajeDisponible){
			int realizarSabotaje = random.nextInt(100); // probabilidad de realizar sabotaje
			if (realizarSabotaje >= 0 && realizarSabotaje < 3){
				 addPercept(agName, Literal.parseLiteral("intencion_sabotear_ox(impostor)"));
				 model.setSaboteoDisponible(false);
				 model.setOxigenoSaboteado(true);
			} else if (realizarSabotaje >= 3 && realizarSabotaje < 6) {
				 addPercept(agName, Literal.parseLiteral("intencion_sabotear_re(impostor)"));
				 model.setSaboteoDisponible(false);
				 model.setReactorSaboteado(true);
			}
		}
	}
	

	private int getAgIdBasedOnName(String agName) {
		
		if (agName.startsWith("tripulante"))
			return (Integer.parseInt(agName.substring(agName.length()-1))) - 1;
		else 
			return model.getNumTripulantes();

	}
	
}
