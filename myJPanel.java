/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FlightSchedulerRobertYan_rjy5060;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 *
 * @author Admin
 */
public class myJPanel extends JPanel{
    private BufferedImage bg;
    private String fileName;
    
    public myJPanel(String img){
        fileName="resources/"+img;
        try{
            bg=ImageIO.read(new File(fileName));
        }catch(IOException e){         
        }
    }
    
    @Override
    public void paintComponent(Graphics g){
        
        super.paintComponent(g);
            g.drawImage(bg, 0, 0, 900, 600, null);
    }
}
