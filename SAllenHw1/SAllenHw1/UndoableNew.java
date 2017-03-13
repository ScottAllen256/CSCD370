/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Hw1Scribble2;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javax.swing.undo.AbstractUndoableEdit;

/**
 *
 * @author sallen
 */
public class UndoableNew extends AbstractUndoableEdit {
    
    
    private final Canvas mLiveCanvas;
    private final Canvas mCanvasCpy;
    
    public UndoableNew(Canvas sceneCanvas) {
        mLiveCanvas = sceneCanvas;
        mCanvasCpy = duplicateCanvas(sceneCanvas);
    }
    
    private Canvas duplicateCanvas(Canvas canvasOrig) { 
        WritableImage wI = canvasOrig.snapshot(null, null);
        Canvas tempCanvas = new Canvas(canvasOrig.getHeight(), canvasOrig.getWidth());
        tempCanvas.getGraphicsContext2D().drawImage(wI, 0, 0);
        return tempCanvas;
    }
    
    @Override
    public void undo() {
        Canvas tempCanvas = duplicateCanvas(mLiveCanvas);
        copyCanvas(mCanvasCpy, mLiveCanvas);
        copyCanvas(tempCanvas, mCanvasCpy);
    }
    
    @Override
    public void redo() {
        Canvas tempCanvas = duplicateCanvas(mLiveCanvas);
        copyCanvas(mCanvasCpy, mLiveCanvas);        
        copyCanvas(tempCanvas, mCanvasCpy);
    }
    
    private void copyCanvas(Canvas cpCanvas, Canvas lCanvas) {
        WritableImage wI = cpCanvas.snapshot(null, null);
        lCanvas.getGraphicsContext2D().drawImage(wI, 0, 0);
    }
    
    @Override
    public boolean canRedo() {
        return true;
    }
    
    @Override
    public String getPresentationName() {
        return "New";
    }
    
}
