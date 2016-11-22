package gui.softwareMeasurement.structureBrowser;

import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

public class ScrollComboBox<T> extends JComboBox<T> {

     public ScrollComboBox() {
         super();
         setUI(new ComboUI());
     }
     
     public ScrollComboBox(Vector<T> vec) {
    	 super(vec);
    	 setUI(new ComboUI());
     }
     
     class ComboUI extends BasicComboBoxUI {
         protected ComboPopup createPopup() {
        	 BasicComboPopup popup = new BasicComboPopup(comboBox) {
        		 protected JScrollPane createScroller() {
        			 return new JScrollPane(list,
        					 ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        					 ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        			 }
        		 };
        	return popup;
         }
     }
}
