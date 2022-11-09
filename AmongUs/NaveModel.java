package AmongUs;

import java.util.Random;
import java.util.logging.Logger;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;


public class NaveModel extends GridWorldModel {
		
		public static Logger logger 					= Logger.getLogger(NaveEnv.class.getName());
	    public static final int GSize 				= 8; // tamaño grid
		public static final int TAREA 				= 16; // codigo tarea sin realizar
		public static final int TAREA_COMPLETADA  	= 32; // codigo tarea completada
		
		public static final int OXIGENO  			= 64; // codigo oxigeno
		public static final int REACTOR  			= 128; // codigo reactor
		public static final int OXIGENO_SABOTEADO  	= 256; // codigo oxigeno (estado = saboteado)
		public static final int REACTOR_SABOTEADO 	= 512; // codigo reactor (estado = saboteado)
		
        public static final int MErr 				= 2; // error maximo al realizar una tarea
        
		private int nerr; // numero de errores al realizar tarea
		
		private Location tarea_mas_cercana;
		private Location reactor_loc;
		private Location oxigeno_loc;
 		
		private boolean saboteoDisponible;
		private boolean oxigenoSaboteado;
		private boolean reactorSaboteado;
		
		private Random random = new Random(System.currentTimeMillis());
		
		private int num_tripulantes, num_impostores;
		
        public NaveModel(int num_tripulantes, int num_impostores) {
            
			super(GSize, GSize, num_tripulantes + num_impostores);
		
            try {
				
				this.num_tripulantes = num_tripulantes;
				this.num_impostores = num_impostores;
				
				/*
				int x1 = random.nextInt(GSize);
				int y1 = random.nextInt(GSize);
				
				int x2 = random.nextInt(GSize);
				int y2 = random.nextInt(GSize);
				
                	setAgPos(0, x1, y1);
				setAgPos(1, x2, y2);
				*/
				
				this.tarea_mas_cercana = new Location((GSize-1)/2,(GSize-1)/2);
				
				// crear 3 tareas por tripulante
				crearTareas(this.num_tripulantes*3);
				
				// crear el reactor y el oxigeno
				reactor_loc = new Location(GSize-1, GSize-1);
				oxigeno_loc = new Location((GSize-1)/2, 0);
				add(OXIGENO, oxigeno_loc);
				add(REACTOR, reactor_loc);
				
				this.saboteoDisponible = true;
				this.oxigenoSaboteado = false;
				this.reactorSaboteado = false;
				
			} catch (Exception e) {
                e.printStackTrace();
            }
        }
		
		public Location getOxigenoLocation() {
			return oxigeno_loc;
		}
		
		public Location getReactorLocation() {
			return reactor_loc;
		}
		
		public boolean getSaboteoDisponible() {
			return saboteoDisponible;
		}
		
		public boolean getOxigenoSaboteado() {
			return oxigenoSaboteado;
		}
		public boolean getReactorSaboteado() {
			return reactorSaboteado;
		}
		
		public void setSaboteoDisponible(boolean saboteo) {
			this.saboteoDisponible = saboteo;
		}
		
		public void setOxigenoSaboteado(boolean estado) {
			this.oxigenoSaboteado = estado;
		}
		
		public void setReactorSaboteado(boolean estado) {
			this.reactorSaboteado = estado;
		}
		
		private void crearTareas(int n_tareas) {
			boolean[][] nave = new boolean[GSize][GSize];
			int x,y;
			Random r = new Random(System.currentTimeMillis());
			for(int i=0; i < n_tareas; i++){
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

		
		// Acciones
		
        void nextSlot_crewmate(int agId) throws Exception {
            Location pos = getAgPos(agId);
            pos.x ++;
            if (pos.x == getWidth()) {
                pos.x = 0;
                pos.y++;
            }
            // vuelve al inicio
            if (pos.y == getHeight()) {
                pos.y = 0;
                pos.x++;
            }
			
            setAgPos(agId, pos);
        }
		
		void nextSlot_impostor(int agId) throws Exception {
			Location pos = getAgPos(agId);
			
			pos.x++;

        		if (pos.x == getWidth()) {
                pos.x = 0;
                pos.y++;
            }
            // vuelve al inicio
            if (pos.y == getHeight()) {
                pos.y = 0;
                pos.x++;
            }
			setAgPos(agId, pos);
        }
		

        void moveTowards(int x, int y, int agId) throws Exception {
            Location pos = getAgPos(agId);
            if (pos.x < x)
                pos.x++;
            else if (pos.x > x)
                pos.x--;
            if (pos.y < y)
                pos.y++;
            else if (pos.y > y)
                pos.y--;
            setAgPos(agId, pos);
        }


        void realizar_tarea(int agId) {
            if (this.hasObject(TAREA, getAgPos(agId))) {
                // la realizacion de la tarea puede fallar
                if (true) {
                    remove(TAREA, getAgPos(agId));
				    add(TAREA_COMPLETADA, getAgPos(agId));
					logger.info("Tarea completada");
                    	nerr = 0;
                } else {
					logger.info("Ha fallado la realizacion de la tarea");
					logger.info("Volviendo a intentar la tarea");
                    nerr++;
                }
            }
        }
		
		
		void sabotear_oxigeno() {
			logger.info("saboteo en OXIGENO ");
                if (true) {
                    remove(OXIGENO, oxigeno_loc);
					add(OXIGENO_SABOTEADO, oxigeno_loc);
					logger.info("OXIGENO SABOTEADO");
                } else {
					logger.info("Ha fallado la realizacion del sabotaje");
					logger.info("Volviendo a intentar el sabotaje");
                }
            
        }
		
		void arreglar_oxigeno() {
			if (this.hasObject(OXIGENO_SABOTEADO, oxigeno_loc)) {
				// la realizacion del sabotaje puede fallar
				if (true) {
					remove(OXIGENO_SABOTEADO,oxigeno_loc);
					add(OXIGENO, oxigeno_loc);
					logger.info("Oxigeno arreglado");
				} else {
					logger.info("Ha fallado el arreglo del oxigeno");
					logger.info("Volviendo a intentar arreglar oxigeno");
				}
			}
		}
		
		void sabotear_reactor() {
			logger.info("saboteo en REACTOR");
                if (true) {
                    remove(REACTOR, reactor_loc);
					add(REACTOR_SABOTEADO, reactor_loc);
					logger.info("REACTOR SABOTEADO");
                } else {
					logger.info("Ha fallado la realizacion del sabotaje");
					logger.info("Volviendo a intentar el sabotaje");
                }
            
        }
		
		void arreglar_reactor() {
			if (this.hasObject(REACTOR_SABOTEADO, reactor_loc)) {
				// la realizacion del sabotaje puede fallar
				if (true) {
					remove(REACTOR_SABOTEADO,reactor_loc);
					add(REACTOR, reactor_loc);
					logger.info("Reactor arreglado");
				} else {
					logger.info("Ha fallado el arreglo del reactor");
					logger.info("Volviendo a intentar arreglar reactor");
				}
			}
		}
		
		// devuelve la distancia euclidea entre dos puntos
		private double distance (int x1, int y1, int x2, int y2) {
            double x = Math.pow(x2 - x1, 2);
            double y = Math.pow(y2 - y1, 2);
            return Math.sqrt(x+y);
        }
		
		void asignar_nueva_tarea(int agId) {
			
            Location r1 = getAgPos(agId);
            int [][] nave = this.data;
            int [] nearestTask = {(GSize-1)/2,(GSize-1)/2}; // si no quedan tareas vuelve al centro
            double nearestDistance = 9999;
            double dist;
            for (int i = 0; i < nave.length; i++){
                for (int j = 0; j < nave[i].length; j++){
                    if (nave[i][j] == TAREA) {
                        dist = distance(r1.x, r1.y, i, j);
                        if (dist < nearestDistance) {
                            nearestDistance = dist;
                            nearestTask[0] = i;
                            nearestTask[1] = j;
                        }
                    }
                }
            }
            	this.tarea_mas_cercana = new Location(nearestTask[0], nearestTask[1]);
        }

    }
