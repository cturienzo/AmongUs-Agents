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

public class Nave extends Environment {

    public static final int GSize = 16; // tamaño grid
    public static final int TAREA = 16; // codigo tarea sin realizar
	public static final int TAREA_COMPLETADA  = 32; // codigo tarea completada
	
	public static final int OXIGENO  = 64; // codigo tarea completada
	public static final int REACTOR  = 128; // codigo tarea completada
	public static final int OXIGENO_SABOTEADO  = 256; // codigo tarea completada
	public static final int REACTOR_SABOTEADO  = 512; // codigo tarea completada

	
    public static final Term ns_crew = Literal.parseLiteral("next_crew(slot)");
	public static final Term ns_imp = Literal.parseLiteral("next_imp(slot)");

	
	public static final Term rt = Literal.parseLiteral("realizar_tarea(tarea)");
	public static final Term st = Literal.parseLiteral("sabotear(tarea_completada)");
	
	public static final Term so = Literal.parseLiteral("sabotear_oxigeno(oxigeno)");
	public static final Term ao = Literal.parseLiteral("arreglar_oxigeno(oxigeno)");
	
	public static final Term sr = Literal.parseLiteral("sabotear_reactor(reactor)");
	public static final Term ar = Literal.parseLiteral("arreglar_reactor(reactor)");


    public static final Literal g1 = Literal.parseLiteral("tarea(r1)");
    public static final Literal g2 = Literal.parseLiteral("tarea_completada(r2)");
	
	public static final Literal int_sab_ox = Literal.parseLiteral("intencion_sabotear_ox(r2)");
	public static final Literal int_sab_re = Literal.parseLiteral("intencion_sabotear_re(r2)");


	public static final Literal ox_sab = Literal.parseLiteral("oxigeno_saboteado(pos_ox)");
	public static final Literal re_sab = Literal.parseLiteral("reactor_saboteado(pos_re)");


    static Logger logger = Logger.getLogger(Nave.class.getName());

    private NaveModel model;
    private NaveView view;
	
	public int pos_x_ox = 0;
	public int pos_y_ox = 0;
	
	public int pos_x_re = 0;
	public int pos_y_re = 0; 
	
    @Override
    public void init(String[] args) {
        model = new NaveModel();
        view  = new NaveView(model);
        model.setView(view);
        updatePercepts();
    }

    @Override
    public boolean executeAction(String ag, Structure action) {
        logger.info(ag+" doing: "+ action);
        try {
            if (action.equals(ns_crew)) {
                model.nextSlot_crewmate();
			}else if(action.equals(ns_imp)){
				model.nextSlot_impostor();
            } else if (action.getFunctor().equals("move_towards")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                model.moveTowards(x,y);
            } else if (action.equals(rt)) {
                model.realizar_tarea();      
            }else if (action.equals(st)) {
                model.sabotear();
            }else if (action.equals(so)) {
                model.sabotear_oxigeno();
            }else if (action.equals(ao)) {
                model.arreglar_oxigeno();
            }else if (action.equals(sr)) {
                model.sabotear_reactor();
            }else if (action.equals(ar)) {
                model.arreglar_reactor();
            }else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        updatePercepts();

        try {
            Thread.sleep(200);
        } catch (Exception e) {}
        informAgsEnvironmentChanged();
        return true;
    }

    /** creates the agents perception based on the NaveModel */
    void updatePercepts() {
        clearPercepts();
		
		Random random = new Random(System.currentTimeMillis());
		
        Location r1Loc = model.getAgPos(0);
		Location r2Loc = model.getAgPos(1);

        Literal pos1 = Literal.parseLiteral("pos(r1," + r1Loc.x + "," + r1Loc.y + ")");
		Literal pos2 = Literal.parseLiteral("pos(r2," + r2Loc.x + "," + r2Loc.y + ")");
		Literal pos_ox = Literal.parseLiteral("pos(ox," + pos_x_ox + "," + pos_y_ox + ")");
		Literal pos_re = Literal.parseLiteral("pos(re," + pos_x_re + "," + pos_y_re + ")");



        addPercept("r1",pos1);
		addPercept("r2",pos2);
		addPercept(pos_ox);
		addPercept(pos_re);


        if (model.hasObject(TAREA, r1Loc)) {
            addPercept("r1",g1);
        }
		if (model.hasObject(TAREA_COMPLETADA, r1Loc)) {
            removePercept("r1",g1);
        }
		
		
		int realizarSabotaje_ox = random.nextInt(100);
		int realizarSabotaje_re = random.nextInt(1000);

		logger.info("El valor de realizarSabotaje de oxigeno es: " + realizarSabotaje_ox);
		logger.info("El valor de realizarSabotaje de reactor es: " + realizarSabotaje_re);

		boolean oxigeno_saboteado = model.hasObject(OXIGENO_SABOTEADO, pos_x_ox, pos_y_ox);
		boolean reactor_saboteado = model.hasObject(REACTOR_SABOTEADO, pos_x_re, pos_y_re);
		
		if (realizarSabotaje_ox<10 && !oxigeno_saboteado && !reactor_saboteado) {
            addPercept("r2",int_sab_ox);
			oxigeno_saboteado = true;
        }
		if (oxigeno_saboteado) {
			logger.info("Soy r1 y tengo la percepción de oxígeno saboteado");
            addPercept("r1",ox_sab);
        }
		
		if (realizarSabotaje_re<10 && !oxigeno_saboteado && !reactor_saboteado) {
            addPercept("r2",int_sab_re);
			reactor_saboteado = true;
        }
		
		
		if (reactor_saboteado) {
			logger.info("Soy r1 y tengo la percepción de reactor saboteado");
            addPercept("r1",re_sab);
        }
		
		

    }

    class NaveModel extends GridWorldModel {

        public static final int MErr = 2; // max error in pick garb
        int nerr; // number of tries of pick garb

        Random random = new Random(System.currentTimeMillis());

        private NaveModel() {
            super(GSize, GSize, 2);
		
            // initial location of agents
            try {
				int x1 = random.nextInt(GSize);
				int y1 = random.nextInt(GSize);
				
				int x2 = random.nextInt(GSize);
				int y2 = random.nextInt(GSize);
				
                setAgPos(0, x1, y1);
				setAgPos(1, x2, y2);

            } catch (Exception e) {
                e.printStackTrace();
            }
	
			
            // crear n_tareas
			crearTareas(8);
			 int x_ox = random.nextInt(GSize);
			 int y_ox = random.nextInt(GSize);
			 
			 pos_x_ox = x_ox;
			 pos_y_ox = y_ox;

	
			 int x_re = random.nextInt(GSize);
			 int y_re = random.nextInt(GSize);
			 
			 pos_x_re = x_re;
			 pos_y_re = y_re;
			 
			add(OXIGENO, pos_x_ox, pos_y_ox);
			add(REACTOR, pos_x_re, pos_y_re);
        }
		
		void crearTareas(int n_tareas) {
			boolean[][] nave = new boolean[GSize][GSize];
			int x,y;
			Random r = new Random(System.currentTimeMillis());
			for(int i=0; i<n_tareas; i++){
				x = r.nextInt(GSize);
				y = r.nextInt(GSize);
				while (nave[x][y]){
					x = r.nextInt(GSize);
					y = r.nextInt(GSize);
				}
				add(TAREA, x, y);
				nave[x][y] = true;
			}
		}

        void nextSlot_crewmate() throws Exception {
            Location r1 = getAgPos(0);
            r1.x ++;
            if (r1.x == getWidth()) {
                r1.x = 0;
                r1.y++;
            }
            // finished searching the whole grid
            if (r1.y == getHeight()) {
                r1.y = 0;
                r1.x++;
            }
			
            setAgPos(0, r1);
        }
		
		void nextSlot_impostor() throws Exception {
			Location r2 = getAgPos(1);
			
			r2.x++;

        		if (r2.x == getWidth()) {
                r2.x = 0;
                r2.y++;
            }
            // finished searching the whole grid
            if (r2.y == getHeight()) {
                r2.y = 0;
                r2.x++;
            }
			setAgPos(1, r2);
        }
		
		

        void moveTowards(int x, int y) throws Exception {
            Location r1 = getAgPos(0);
            if (r1.x < x)
                r1.x++;
            else if (r1.x > x)
                r1.x--;
            if (r1.y < y)
                r1.y++;
            else if (r1.y > y)
                r1.y--;
            setAgPos(0, r1);
        }

        void realizar_tarea() {
            // hay una tarea en la posicion del agente 0
            if (model.hasObject(TAREA, getAgPos(0))) {
                // la realizacion de la tarea puede fallar
                if (true) {
                    remove(TAREA, getAgPos(0));
				    add(TAREA_COMPLETADA, getAgPos(0));
					logger.info("Tarea completada");
                    nerr = 0;
                } else {
					logger.info("Ha fallado la realizacion de la tarea");
					logger.info("Volviendo a intentar la tarea");
                    nerr++;
                }
            }
        }
		
	void sabotear() {
		logger.info("saboteo en: " + getAgPos(1));
		// hay una tarea en la posicion del agente 1
		if (model.hasObject(TAREA_COMPLETADA, getAgPos(1))) {
			// la realizacion del sabotaje puede fallar
			if (true) {
				remove(TAREA_COMPLETADA, getAgPos(1));
				add(TAREA, getAgPos(1));
				logger.info("Sabotaje completado");
			} else {
				logger.info("Ha fallado la realizaci�n del sabotaje");
				logger.info("Volviendo a intentar el sabotaje");
			}
		}
	}
		
	void sabotear_oxigeno() {
			logger.info("saboteo en OXIGENO ");
                if (true) {
                    remove(OXIGENO, pos_x_ox, pos_y_ox);
					add(OXIGENO_SABOTEADO, pos_x_ox, pos_y_ox);
					logger.info("OXIGENO SABOTEADO");
                } else {
					logger.info("Ha fallado la realizaci�n del sabotaje");
					logger.info("Volviendo a intentar el sabotaje");
                }
            
        }
			void arreglar_oxigeno() {
			if (model.hasObject(OXIGENO_SABOTEADO, pos_x_ox, pos_y_ox)) {
				// la realizacion del sabotaje puede fallar
				if (true) {
					remove(OXIGENO_SABOTEADO, pos_x_ox, pos_y_ox);
					add(OXIGENO, pos_x_ox, pos_y_ox);
					logger.info("Oxigeno arreglado");
				} else {
					logger.info("Ha fallado el arreglo del oxigeno");
					logger.info("Volviendo a intentar arreglar oxigeno");
				}
			}
		}
		
		void sabotear_reactor() {
			logger.info("saboteo en REACTOR ");
                if (true) {
                    remove(REACTOR, pos_x_re, pos_y_re);
					add(REACTOR_SABOTEADO, pos_x_re, pos_y_re);
					logger.info("REACTOR SABOTEADO");
                } else {
					logger.info("Ha fallado la realizacion del sabotaje");
					logger.info("Volviendo a intentar el sabotaje");
                }
            
        }
			void arreglar_reactor() {
			if (model.hasObject(REACTOR_SABOTEADO, pos_x_re, pos_y_re)) {
				// la realizacion del sabotaje puede fallar
				if (true) {
					remove(REACTOR_SABOTEADO, pos_x_re, pos_y_re);
					add(REACTOR, pos_x_re, pos_y_re);
					logger.info("Reactor arreglado");
				} else {
					logger.info("Ha fallado el arreglo del reactor");
					logger.info("Volviendo a intentar arreglar reactor");
				}
			}
		}

    }
	

    class NaveView extends GridWorldView {

        public NaveView(NaveModel model) {
            super(model, "Among Us", 600);
            defaultFont = new Font("Arial", Font.BOLD, 18); // change default font
            setVisible(true);
            repaint();
        }

        /** draw application objects */
        @Override
        public void draw(Graphics g, int x, int y, int object) {
			switch (object) {
				case Nave.TAREA:
					drawTarea(g, x, y, Nave.TAREA);
					break;
				case Nave.TAREA_COMPLETADA:
					drawTarea(g, x, y, Nave.TAREA_COMPLETADA);
					break;
				case Nave.OXIGENO:
					drawTarea(g, x, y, Nave.OXIGENO);
					break;
				case Nave.REACTOR:
					drawTarea(g, x, y, Nave.REACTOR);
					break;
				case Nave.OXIGENO_SABOTEADO:
					drawTarea(g, x, y, Nave.OXIGENO_SABOTEADO);
					break;
				case Nave.REACTOR_SABOTEADO:
					drawTarea(g, x, y, Nave.REACTOR_SABOTEADO);
					break;
            }
        }

        @Override
        public void drawAgent(Graphics g, int x, int y, Color c, int id) {
            String label = "R"+(id+1);
            c = Color.blue;
            if (id == 0) {
                c = Color.yellow;				
            }else{
				c = Color.black;
			}
            super.drawAgent(g, x, y, c, -1);
            if (id == 0) {
                g.setColor(Color.black);
            } else {
                g.setColor(Color.white);
            }
            super.drawString(g, x, y, defaultFont, label);
            repaint();
        }

        public void drawTarea(Graphics g, int x, int y, int estado_tarea) {
			
			if(estado_tarea==Nave.TAREA){
				g.setColor(Color.red);
				g.fillOval(x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8);

			}else if (estado_tarea == Nave.TAREA_COMPLETADA){
				g.setColor(Color.green);
				g.fillOval(x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8);
			}else if (estado_tarea == Nave.OXIGENO){
				g.setColor(Color.blue);
				g.fillOval(x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8);
				
				g.setColor(Color.white);
				String label = "OX";
				super.drawString(g, x, y, defaultFont, label);
			}
			else if (estado_tarea == Nave.REACTOR){
				g.setColor(Color.orange);
				g.fillOval(x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8);
				
				g.setColor(Color.white);
				String label = "R";
				super.drawString(g, x, y, defaultFont, label);
			}
			else if (estado_tarea == Nave.OXIGENO_SABOTEADO){
				g.setColor(Color.red);
				g.fillOval(x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8);
				
				g.setColor(Color.white);
				String label = "OX";
				super.drawString(g, x, y, defaultFont, label);
			}
			else if (estado_tarea == Nave.REACTOR_SABOTEADO){
				g.setColor(Color.red);
				g.fillOval(x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8);
				
				g.setColor(Color.white);
				String label = "R";
				super.drawString(g, x, y, defaultFont, label);
			}
				
            
        }

    }
}
