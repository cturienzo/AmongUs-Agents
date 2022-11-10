package AmongUs;

import jason.environment.Environment;
import jason.environment.grid.GridWorldView;

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.imageio.ImageIO;

import java.io.IOException;


public class NaveView extends GridWorldView {
		
		private BufferedImage crewmate_image;
		private BufferedImage imposter_image;
		private BufferedImage reactor_image;
		private BufferedImage oxygen_image;
		private BufferedImage tarea_image;
		private BufferedImage tarea_completada_image;
		
		private NaveModel model;
		
        public NaveView(NaveModel model) {
            super(model, "Among Us", 800);
			this.model = model;
            defaultFont = new Font("Arial", Font.BOLD, 12); // change default font
            setVisible(true);
			try {
				
				crewmate_image = ImageIO.read(getClass().getResourceAsStream("../images/crewmate.png"));
				imposter_image = ImageIO.read(getClass().getResourceAsStream("../images/imposter.png"));
				reactor_image = ImageIO.read(getClass().getResourceAsStream("../images/reactor.png"));
				oxygen_image = ImageIO.read(getClass().getResourceAsStream("../images/oxygen.png"));
				tarea_image = ImageIO.read(getClass().getResourceAsStream("../images/tarea.png"));
				tarea_completada_image = ImageIO.read(getClass().getResourceAsStream("../images/tarea_completada.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
            repaint();
        }

        /** draw application objects */
        @Override
        public void draw(Graphics g, int x, int y, int object) {
			switch (object) {
				case NaveModel.TAREA:
					drawElemento(g, x, y, NaveModel.TAREA);
					break;
				case NaveModel.TAREA_COMPLETADA:
					drawElemento(g, x, y, NaveModel.TAREA_COMPLETADA);
					break;
				case NaveModel.OXIGENO:
					drawElemento(g, x, y, NaveModel.OXIGENO);
					break;
				case NaveModel.REACTOR:
					drawElemento(g, x, y, NaveModel.REACTOR);
					break;
				case NaveModel.OXIGENO_SABOTEADO:
					drawElemento(g, x, y, NaveModel.OXIGENO_SABOTEADO);
					break;
				case NaveModel.REACTOR_SABOTEADO:
					drawElemento(g, x, y, NaveModel.REACTOR_SABOTEADO);
					break;
            }
        }

        @Override
        public void drawAgent(Graphics g, int x, int y, Color c, int id) {
            String label;
			int n_tripulantes = model.getNumTripulantes();
			
			if (id < n_tripulantes) { // dibujar tripulantes
				g.drawImage(crewmate_image,x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8, null);
				super.drawString(g, x * cellSizeW+12, y * cellSizeH+12, defaultFont, "T"+id);
            
			} else { // dibujar impostores
			
				g.drawImage(imposter_image,x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8, null);
			
			}
            repaint();
        }

        public void drawElemento(Graphics g, int x, int y, int id_elemento) {
			
			if(id_elemento==NaveModel.TAREA){
				g.drawImage(tarea_image,x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8, null);

			} else if (id_elemento == NaveModel.TAREA_COMPLETADA){
				
				g.drawImage(tarea_completada_image,x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8, null);
			
			} else if (id_elemento == NaveModel.OXIGENO){
				
				g.drawImage(oxygen_image,x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8, null);
			
			} else if (id_elemento == NaveModel.REACTOR){
				
				g.drawImage(reactor_image,x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8, null);

			} else if (id_elemento == NaveModel.OXIGENO_SABOTEADO){
				
				g.drawImage(oxygen_image,x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8, Color.red, null);

			} else if (id_elemento == NaveModel.REACTOR_SABOTEADO){

				g.drawImage(reactor_image,x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8, Color.red, null);

			}	
			
			repaint();
            
        }

    }
