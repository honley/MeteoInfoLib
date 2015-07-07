 /* Copyright 2012 - Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.data.meteodata.bandraster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.mapdata.geotiff.GeoTiff;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.data.meteodata.Dimension;
import org.meteoinfo.data.meteodata.DimensionType;
import org.meteoinfo.data.meteodata.IGridDataInfo;
import org.meteoinfo.data.meteodata.MeteoDataType;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.data.meteodata.ascii.ASCIIGridDataInfo;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.ma2.Section;

/**
 *
 * @author yaqiang
 */
public class GeoTiffDataInfo extends DataInfo implements IGridDataInfo {

    // <editor-fold desc="Variables">
    private GeoTiff geoTiff;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    public GeoTiffDataInfo() {
        this.setDataType(MeteoDataType.GEOTIFF);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    @Override
    public void readDataInfo(String fileName) {
        try {
            this.setFileName(fileName);
            
            geoTiff = new GeoTiff(fileName);
            geoTiff.read();
            List<double[]> xy = geoTiff.readXY();
            double[] X = xy.get(0);
            double[] Y = xy.get(1);
            Dimension xDim = new Dimension(DimensionType.X);
            xDim.setValues(X);
            this.setXDimension(xDim);
            Dimension yDim = new Dimension(DimensionType.Y);
            yDim.setValues(Y);
            this.setYDimension(yDim);
            
            List<Variable> variables = new ArrayList<>();
            Variable aVar = new Variable();
            aVar.setName("var");
            aVar.addDimension(yDim);
            aVar.addDimension(xDim);            
            variables.add(aVar);
            this.setVariables(variables);
            
            this.setProjectionInfo(geoTiff.readProj());
        } catch (IOException ex) {
            Logger.getLogger(GeoTiffDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String generateInfoText() {
        String dataInfo;
        dataInfo = "File Name: " + this.getFileName();
        dataInfo += System.getProperty("line.separator") + "Data Type: GeoTiff";
        Dimension xdim = this.getXDimension();
        Dimension ydim = this.getYDimension();
        dataInfo += System.getProperty("line.separator") + "XNum = " + String.valueOf(xdim.getDimLength())
                + "  YNum = " + String.valueOf(ydim.getDimLength());
        dataInfo += System.getProperty("line.separator") + "XMin = " + String.valueOf(xdim.getValues()[0])
                + "  YMin = " + String.valueOf(ydim.getValues()[0]);
        dataInfo += System.getProperty("line.separator") + "XSize = " + String.valueOf(xdim.getValues()[1] - xdim.getValues()[0])
                + "  YSize = " + String.valueOf(ydim.getValues()[1] - ydim.getValues()[0]);
        dataInfo += System.getProperty("line.separator") + "UNDEF = " + String.valueOf(this.getMissingValue());
        dataInfo += System.getProperty("line.separator") + "Variable = " + this.getVariableNames().get(0);

        return dataInfo;
    }
    
    /**
     * Read array data of the variable
     *
     * @param varName Variable name
     * @param origin The origin array
     * @param size The size array
     * @param stride The stride array
     * @return Array data
     */
    @Override
    public Array read(String varName, int[] origin, int[] size, int[] stride) {
        try {
            Section section = new Section(origin, size, stride);
            Array dataArray = Array.factory(DataType.FLOAT, section.getShape());
            int rangeIdx = 0;
            Range yRange = section.getRange(rangeIdx++);
            Range xRange = section.getRange(rangeIdx);
            IndexIterator ii = dataArray.getIndexIterator();
            readXY(yRange, xRange, ii);

            return dataArray;
        } catch (InvalidRangeException ex) {
            Logger.getLogger(ASCIIGridDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    private void readXY(Range yRange, Range xRange, IndexIterator ii) {
        int[][] data = this.geoTiff.readData();
        for (int y = yRange.first(); y <= yRange.last();
                y += yRange.stride()) {
            for (int x = xRange.first(); x <= xRange.last();
                    x += xRange.stride()) {
                ii.setFloatNext(data[y][x]);
            }
        }
    }

    @Override
    public GridData getGridData_LonLat(int timeIdx, int varIdx, int levelIdx) {
        GridData gdata = this.geoTiff.getGridData_Value();
        gdata.xArray = this.getXDimension().getValues();
        gdata.yArray = this.getYDimension().getValues();
        gdata.projInfo = this.getProjectionInfo();
        
        return gdata;
    }
    
    @Override
    public GridData getGridData_TimeLat(int lonIdx, int varIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_TimeLon(int latIdx, int varIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_LevelLat(int lonIdx, int varIdx, int timeIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_LevelLon(int latIdx, int varIdx, int timeIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_LevelTime(int latIdx, int varIdx, int lonIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Time(int lonIdx, int latIdx, int varIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Level(int lonIdx, int latIdx, int varIdx, int timeIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Lon(int timeIdx, int latIdx, int varIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Lat(int timeIdx, int lonIdx, int varIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    // </editor-fold>
}
