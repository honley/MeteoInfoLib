/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.map;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.undo.AbstractUndoableEdit;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.PointD;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.shape.Graphic;
import org.meteoinfo.shape.Shape;
import org.meteoinfo.table.DataRow;

/**
 *
 * @author yaqiang
 */
public class MapViewUndoRedo {
    // <editor-fold desc="Undo/Redo">
    public class ZoomEdit extends AbstractUndoableEdit {
        MapView mapView;
        Extent newExtent;
        Extent oldExtent;
        
        public ZoomEdit(MapView mapView, Extent oldExtent, Extent newExtent){
            this.mapView = mapView;
            this.newExtent = newExtent;
            this.oldExtent = oldExtent;
        }
        
        @Override
        public String getPresentationName() {
            return "Zoom";
        }
        
        @Override
        public void undo() {
            super.undo();
            mapView.zoomToExtent(oldExtent);
        }
        
        @Override
        public void redo(){
            super.redo();
            mapView.zoomToExtent(newExtent);
        }
    }
    
    class AddFeatureEdit extends FeatureUndoableEdit {
        MapView mapView;
        Shape shape;
        VectorLayer layer;
        
        public AddFeatureEdit(MapView mapView, VectorLayer layer, Shape shape){
            this.mapView = mapView;
            this.layer = layer;
            this.shape = shape;
        }
        
        @Override
        public String getPresentationName() {
            return "Add a Feature";
        }
        
        @Override
        public void undo() {
            super.undo();
            layer.editRemoveShape(shape);
            mapView.paintLayers();
            System.out.println("Undo add a feature");
        }
        
        @Override
        public void redo(){
            super.redo();
            try {
                layer.editAddShape(shape);
                mapView.paintLayers();
                System.out.println("Redo add a feature");
            } catch (Exception ex) {
                Logger.getLogger(MapView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }                
    }
    
    public class RemoveFeaturesEdit extends FeatureUndoableEdit {
        MapView mapView;
        List<Shape> shapes;
        VectorLayer layer;
        List<Integer> indices;
        List<DataRow> records;
        
        public RemoveFeaturesEdit(MapView mapView, VectorLayer layer, List<Shape> shapes){
            this.mapView = mapView;
            this.layer = layer;
            this.shapes = shapes;
            indices = new ArrayList<Integer>();
            records = new ArrayList<DataRow>();
            int idx;
            DataRow row;
            for (Shape shape : shapes){
                idx = layer.getShapes().indexOf(shape);
                row = layer.getAttributeTable().getTable().getRows().get(idx);
                indices.add(idx);
                records.add(row);
            }
        }
        
        @Override
        public String getPresentationName() {
            return "Remove Features";
        }
        
        @Override
        public void undo() {
            super.undo();
            try {
                for (int i = 0; i < shapes.size(); i++){
                    layer.editInsertShape(shapes.get(i), indices.get(i), records.get(i));
                }
                mapView.paintLayers();
                System.out.println("Undo remove features");
            } catch (Exception ex) {
                Logger.getLogger(MapView.class.getName()).log(Level.SEVERE, null, ex);
            }            
        }
        
        @Override
        public void redo(){
            super.redo();            
            for (Shape shape : shapes)
                layer.editRemoveShape(shape);
            mapView.paintLayers();
            System.out.println("Undo remove features");
        }
    }
    
    class MoveFeatureEdit extends FeatureUndoableEdit {
        MapView mapView;
        Shape shape;
        Point fromPoint;
        Point toPoint;
        
        public MoveFeatureEdit(MapView mapView, Shape shape, Point fromPoint, Point toPoint){
            this.mapView = mapView;
            this.shape = shape;
            this.fromPoint = new Point(fromPoint.x, fromPoint.y);
            this.toPoint = new Point(toPoint.x, toPoint.y);
        }
        
        @Override
        public String getPresentationName() {
            return "Move a Feature";
        }
        
        @Override
        public void undo() {
            super.undo();
            mapView.moveShapeOnScreen(shape, toPoint, fromPoint);
            mapView.paintLayers();
        }
        
        @Override
        public void redo(){
            super.redo();
            mapView.moveShapeOnScreen(shape, fromPoint, toPoint);
            mapView.paintLayers();
        }
    }
    
    class MoveFeaturesEdit extends FeatureUndoableEdit {
        MapView mapView;
        List<Shape> shapes;
        Point fromPoint;
        Point toPoint;
        
        public MoveFeaturesEdit(MapView mapView, List<Shape> shapes, Point fromPoint, Point toPoint){
            this.mapView = mapView;
            this.shapes = shapes;
            this.fromPoint = new Point(fromPoint.x, fromPoint.y);
            this.toPoint = new Point(toPoint.x, toPoint.y);
        }
        
        @Override
        public String getPresentationName() {
            return "Move a Feature";
        }
        
        @Override
        public void undo() {
            super.undo();
            for (Shape shape : shapes)
                mapView.moveShapeOnScreen(shape, toPoint, fromPoint);
            mapView.paintLayers();
        }
        
        @Override
        public void redo(){
            super.redo();
            for (Shape shape : shapes)
                mapView.moveShapeOnScreen(shape, fromPoint, toPoint);
            mapView.paintLayers();
        }
    }
    
    class MoveFeatureVerticeEdit extends FeatureUndoableEdit {
        MapView mapView;
        Shape shape;
        int verticeIdx;
        double newX;
        double newY;
        double oldX;
        double oldY;
        
        public MoveFeatureVerticeEdit(MapView mapView, Shape shape, int vIdx, double newX, double newY){
            this.mapView = mapView;
            this.shape = shape;            
            this.verticeIdx = vIdx;
            this.newX = newX;
            this.newY = newY;
            this.oldX = shape.getPoints().get(vIdx).X;
            this.oldY = shape.getPoints().get(vIdx).Y;
        }
        
        @Override
        public String getPresentationName() {
            return "Move a Feature";
        }
        
        @Override
        public void undo() {
            super.undo();
            shape.moveVertice(verticeIdx, oldX, oldY);
            mapView.paintLayers();
        }
        
        @Override
        public void redo(){
            super.redo();
            shape.moveVertice(verticeIdx, newX, newY);
            mapView.paintLayers();
        }
    }
    
    class AddFeatureVerticeEdit extends AbstractUndoableEdit {
        MapView mapView;
        Shape shape;
        int verticeIdx;
        PointD vertice;
        
        public AddFeatureVerticeEdit(MapView mapView, Shape shape, int vIdx, PointD vertice){
            this.mapView = mapView;
            this.shape = shape;            
            this.verticeIdx = vIdx;
            this.vertice = vertice;
        }
        
        @Override
        public String getPresentationName() {
            return "Add feature vertice";
        }
        
        @Override
        public void undo() {
            super.undo();
            shape.removeVerice(verticeIdx);
            mapView.paintLayers();
        }
        
        @Override
        public void redo(){
            super.redo();
            shape.addVertice(verticeIdx, vertice);
            mapView.paintLayers();
        }
    }
    
    class RemoveFeatureVerticeEdit extends AbstractUndoableEdit {
        MapView mapView;
        Shape shape;
        int verticeIdx;
        PointD vertice;
        
        public RemoveFeatureVerticeEdit(MapView mapView, Shape shape, int vIdx){
            this.mapView = mapView;
            this.shape = shape;            
            this.verticeIdx = vIdx;
            this.vertice = shape.getPoints().get(vIdx);
        }
        
        @Override
        public String getPresentationName() {
            return "Remove feature vertice";
        }
        
        @Override
        public void undo() {
            super.undo();
            shape.addVertice(verticeIdx, vertice);
            mapView.paintLayers();
        }
        
        @Override
        public void redo(){
            super.redo();
            shape.removeVerice(verticeIdx);
            mapView.paintLayers();
        }
    }
    
    class AddGraphicEdit extends AbstractUndoableEdit {
        MapView mapView;
        Graphic graphic;
        
        public AddGraphicEdit(MapView mapView, Graphic graphic){
            this.mapView = mapView;
            this.graphic = graphic;
        }
        
        @Override
        public String getPresentationName() {
            return "Add a Graphic";
        }
        
        @Override
        public void undo() {
            super.undo();
            mapView.removeGraphic(graphic);
            mapView.paintLayers();
        }
        
        @Override
        public void redo(){
            super.redo();
            mapView.getGraphicCollection().add(graphic);
            mapView.paintLayers();
        }
    }
    
    class RemoveGraphicEdit extends AbstractUndoableEdit {
        MapView mapView;
        Graphic graphic;
        
        public RemoveGraphicEdit(MapView mapView, Graphic graphic){
            this.mapView = mapView;
            this.graphic = graphic;
        }
        
        @Override
        public String getPresentationName() {
            return "Remove a Graphic";
        }
        
        @Override
        public void undo() {
            super.undo();
            mapView.getGraphicCollection().add(graphic);            
            mapView.paintLayers();
        }
        
        @Override
        public void redo(){
            super.redo();
            mapView.removeGraphic(graphic);
            mapView.paintLayers();
        }
    }
    
    class RemoveGraphicsEdit extends AbstractUndoableEdit {
        MapView mapView;
        List<Graphic> graphics;
        
        public RemoveGraphicsEdit(MapView mapView, List<Graphic> graphics){
            this.mapView = mapView;
            this.graphics = new ArrayList<Graphic>(graphics);
        }
        
        @Override
        public String getPresentationName() {
            return "Remove Graphics";
        }
        
        @Override
        public void undo() {
            super.undo();            
            mapView.getGraphicCollection().addAll(graphics);            
            mapView.paintLayers();
        }
        
        @Override
        public void redo(){
            super.redo();
            mapView.getGraphicCollection().removeAll(graphics);
            mapView.paintLayers();
        }
    }
    
    class MoveGraphicEdit extends AbstractUndoableEdit {
        MapView mapView;
        Graphic graphic;
        Point fromPoint;
        Point toPoint;
        
        public MoveGraphicEdit(MapView mapView, Graphic graphic, Point fromPoint, Point toPoint){
            this.mapView = mapView;
            this.graphic = graphic;
            this.fromPoint = new Point(fromPoint.x, fromPoint.y);
            this.toPoint = new Point(toPoint.x, toPoint.y);
        }
        
        @Override
        public String getPresentationName() {
            return "Move a Graphic";
        }
        
        @Override
        public void undo() {
            super.undo();
            mapView.moveShapeOnScreen(graphic.getShape(), toPoint, fromPoint);
            mapView.paintLayers();
        }
        
        @Override
        public void redo(){
            super.redo();
            mapView.moveShapeOnScreen(graphic.getShape(), fromPoint, toPoint);
            mapView.paintLayers();
        }
    }
    
    class MoveGraphicVerticeEdit extends AbstractUndoableEdit {
        MapView mapView;
        Graphic graphic;
        int verticeIdx;
        double newX;
        double newY;
        double oldX;
        double oldY;
        
        public MoveGraphicVerticeEdit(MapView mapView, Graphic graphic, int vIdx, double newX, double newY){
            this.mapView = mapView;
            this.graphic = graphic;            
            this.verticeIdx = vIdx;
            this.newX = newX;
            this.newY = newY;
            this.oldX = graphic.getShape().getPoints().get(vIdx).X;
            this.oldY = graphic.getShape().getPoints().get(vIdx).Y;
        }
        
        @Override
        public String getPresentationName() {
            return "Move Grahic vertice";
        }
        
        @Override
        public void undo() {
            super.undo();
            graphic.verticeMoveUpdate(verticeIdx, oldX, oldY);
            mapView.paintLayers();
        }
        
        @Override
        public void redo(){
            super.redo();
            graphic.verticeMoveUpdate(verticeIdx, newX, newY);
            mapView.paintLayers();
        }
    }
    
    class AddGraphicVerticeEdit extends AbstractUndoableEdit {
        MapView mapView;
        Graphic graphic;
        int verticeIdx;
        PointD vertice;
        
        public AddGraphicVerticeEdit(MapView mapView, Graphic graphic, int vIdx, PointD vertice){
            this.mapView = mapView;
            this.graphic = graphic;            
            this.verticeIdx = vIdx;
            this.vertice = vertice;
        }
        
        @Override
        public String getPresentationName() {
            return "Add Grahic vertice";
        }
        
        @Override
        public void undo() {
            super.undo();
            graphic.verticeRemoveUpdate(verticeIdx);
            mapView.paintLayers();
        }
        
        @Override
        public void redo(){
            super.redo();
            graphic.verticeAddUpdate(verticeIdx, vertice);
            mapView.paintLayers();
        }
    }
    
    class RemoveGraphicVerticeEdit extends AbstractUndoableEdit {
        MapView mapView;
        Graphic graphic;
        int verticeIdx;
        PointD vertice;
        
        public RemoveGraphicVerticeEdit(MapView mapView, Graphic graphic, int vIdx){
            this.mapView = mapView;
            this.graphic = graphic;            
            this.verticeIdx = vIdx;
            this.vertice = graphic.getShape().getPoints().get(vIdx);
        }
        
        @Override
        public String getPresentationName() {
            return "Remove Grahic vertice";
        }
        
        @Override
        public void undo() {
            super.undo();
            graphic.verticeAddUpdate(verticeIdx, vertice);
            mapView.paintLayers();
        }
        
        @Override
        public void redo(){
            super.redo();
            graphic.verticeRemoveUpdate(verticeIdx);
            mapView.paintLayers();
        }
    }
    
    class ResizeGraphicEdit extends AbstractUndoableEdit {
        MapView mapView;
        Graphic graphic;
        Rectangle oldRect;
        Rectangle newRect;
        
        public ResizeGraphicEdit(MapView mapView, Graphic graphic, Rectangle newRect){
            this.mapView = mapView;
            this.graphic = graphic;
            this.newRect = newRect;
            this.oldRect = mapView.getGraphicRectangle(graphic);
        }
        
        @Override
        public String getPresentationName() {
            return "Resize a Graphic";
        }
        
        @Override
        public void undo() {
            super.undo();
            mapView.resizeShapeOnScreen(graphic, oldRect);
            mapView.paintLayers();
        }
        
        @Override
        public void redo(){
            super.redo();
            mapView.resizeShapeOnScreen(graphic, newRect);
            mapView.paintLayers();
        }
    }
    // </editor-fold>
}