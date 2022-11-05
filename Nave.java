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

    public static final int GSize = 10; // tamaÃ±o grid
    public static final int TAREA = 16; // codigo tarea sin realizar
	public static final int TAREA_COMPLETADA  = 32; // codigo tarea completada


    public static final Term ns = Literal.parseLiteral("next(slot)");
	
	public static final Term rt = Literal.parseLiteral("realizar_tarea(tarea)");
	public static final Term st = Literal.parseLiteral("sabotear(tarea)");


    public static final Literal g1 = Literal.parseLiteral("tarea(r1)");
    public static final Literal g2 = Literal.parseLiteral("tarea_completada(r2)");

    static Logger logger = Logger.getLogger(Nave.class.getName());

    private NaveModel model;
    private NaveView view;

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
            if (action.equals(ns)) {
                model.nextSlot();
            } else if (action.getFunctor().equals("move_towards")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(0)).solve();
                model.moveTowards(x,y);
            } else if (action.equals(rt)) {
                model.realizar_tarea();      
            }else if (action.equals(st)) {
                model.sabotear();
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

        Location r1Loc = model.getAgPos(0);
		Location r2Loc = model.getAgPos(1);

        Literal pos1 = Literal.parseLiteral("pos(r1," + r1Loc.x + "," + r1Loc.y + ")");
		Literal pos2 = Literal.parseLiteral("pos(r2," + r2Loc.x + "," + r2Loc.y + ")");

        addPercept("r1",pos1);
		addPercept("r2",pos2);

        if (model.hasObject(TAREA, r1Loc)) {
            addPercept("r1",g1);
        }
	
		if (model.hasObject(TAREA_COMPLETADA, r2Loc)) {
            addPercept("r2",g2);
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

        void nextSlot() throws Exception {
            Location r1 = getAgPos(0);
			Location r2 = getAgPos(1);
			
            r1.x ++;
			r2.x++;

            if (r1.x == getWidth()) {
                r1.x = 0;
                r1.y++;
            }
            // finished searching the whole grid
            if (r1.y == getHeight()) {
                r1.y = 0;
                r1.x++;
            }
			 if (r2.x == getWidth()) {
                r2.x = 0;
                r2.y++;
            }
            // finished searching the whole grid
            if (r2.y == getHeight()) {
                r2.y = 0;
                r2.x++;
            }
            setAgPos(0, r1);
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
					logger.info("Ha fallado la realización del sabotaje");
					logger.info("Volviendo a intentar el sabotaje");
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

			}else{
				g.setColor(Color.green);
				g.fillOval(x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8);
			}
            
        }

    }
}
