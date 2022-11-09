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
		
        public NaveView(NaveModel model) {
            super(model, "Among Us", 600);
            defaultFont = new Font("Arial", Font.BOLD, 12); // change default font
            setVisible(true);
			try {
				crewmate_image = ImageIO.read(getClass().getResourceAsStream("../images/crewmate.png"));
				imposter_image = ImageIO.read(getClass().getResourceAsStream("../images/imposter.png"));
				reactor_image = ImageIO.read(getClass().getResourceAsStream("../images/reactor.png"));
				oxygen_image = ImageIO.read(getClass().getResourceAsStream("../images/oxygen.png"));

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
			if (id == 0) {
				label = "T"+(id+1);
                	c = Color.blue;
				//super.drawAgent(g, x, y, c, id);
				g.drawImage(crewmate_image,x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8, null);
            } else {
				int impostor_id = id - 1;
				label = "I"+(impostor_id+1);
				c = Color.red;
				//super.drawAgent(g, x, y, c, id);
				g.drawImage(imposter_image,x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8, null);
			}
			//super.drawString(g, x, y, defaultFont, label);
            repaint();
        }

        public void drawElemento(Graphics g, int x, int y, int id_elemento) {
			
			if(id_elemento==NaveModel.TAREA){
				g.setColor(Color.red);
				g.fillOval(x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8);

			}else if (id_elemento == NaveModel.TAREA_COMPLETADA){
				g.setColor(Color.green);
				g.fillOval(x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8);
			}else if (id_elemento == NaveModel.OXIGENO){
				
				/*g.setColor(Color.blue);
				g.fillOval(x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8);
				
				g.setColor(Color.white);
				String label = "OX";
				super.drawString(g, x, y, defaultFont, label);*/
				g.drawImage(oxygen_image,x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8, null);
			}
			else if (id_elemento == NaveModel.REACTOR){
				/*g.setColor(Color.orange);
				g.fillOval(x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8);
				
				g.setColor(Color.white);
				String label = "R";
				super.drawString(g, x, y, defaultFont, label);*/
				g.drawImage(reactor_image,x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8, null);

			}
			else if (id_elemento == NaveModel.OXIGENO_SABOTEADO){
				g.setColor(Color.red);
				g.fillOval(x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8);
				
				g.setColor(Color.white);
				String label = "OX";
				super.drawString(g, x, y, defaultFont, label);
			}
			else if (id_elemento == NaveModel.REACTOR_SABOTEADO){
				g.setColor(Color.red);
				g.fillOval(x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8);
				
				g.setColor(Color.white);
				String label = "R";
				super.drawString(g, x, y, defaultFont, label);
			}
				
            
        }

    }
