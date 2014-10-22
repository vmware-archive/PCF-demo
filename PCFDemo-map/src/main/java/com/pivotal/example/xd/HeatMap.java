package com.pivotal.example.xd;

import java.awt.Color;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeatMap implements Serializable {

	public static String[] states = new String[] {
			   "ca", "ny", "ma", "tx", "il", "wa", "fl", "pa", "va", "nj", "or", "oh", "mi", "co", "md", "nc", "ga", 
			   "mn", "az", "in", "wi", "mo", "tn", "ct", "dc", "ut", "nm", "ks", "ky", "ok", "sc", "la", "nv", "ia", 
			   "nh", "al", "ar", "me", "hi", "ne", "id", "ri", "vt", "mt", "wv", "de", "ak", "ms", "wy", "sd", "nd",
			   "pr", "as"};

	static String commaSeparatedRGBPattern = "^(\\d{1,3}),(\\d{1,3}),(\\d{1,3})$";
    static final String hexaDecimalPattern = "^0x([\\da-fA-F]{1,8})$";
    static final int HEXLENGTH = 8;
	 	
    private Color[] colors;
	
	public Set<HeatMapItem> heatMap;
	
	
	
	public void addOrderSum(String state, int amount){
		HeatMapItem item = new HeatMapItem();
		item.setState(state);
		item.setValue(amount);
		heatMap.add(item);
		
	}
	
	public HeatMap(){
		heatMap = new TreeSet<HeatMapItem>();	
	}
		
	public void assignColors(){
		
		HeatMapItem[] items = (HeatMapItem[])heatMap.toArray(new HeatMapItem[]{});
		int firstValue = items[0].getValue();
		int lastValue = items[items.length-1].getValue();
		
		int delta = lastValue-firstValue;
		colors = interpolate(delta+1); 
		
		Iterator<HeatMapItem> it = heatMap.iterator();
		int i=0;
		while (it.hasNext()){
			HeatMapItem item = it.next();
			int itemColorGradient = item.getValue()-firstValue;
			
			item.setHeatMapColor("#"+convertRGBToHex(colors[itemColorGradient].getRed()+","+colors[itemColorGradient].getGreen()+","+colors[itemColorGradient].getBlue()));
			i++;
		}
		
		
		
	}
	
	
	private Color[] interpolate(int steps){
		
		Color[] colors = new Color[steps];
        
        final Color WHITE = new Color(255,255,255);
        final Color PIVOTAL = new Color(7,63,7);
        
        for (int i = 0; i < steps; i++) {
            
        	float ratio = (float) i / (float) steps;
            int red = (int) (PIVOTAL.getRed() * ratio + WHITE.getRed() * (1 - ratio));
            int green = (int) (PIVOTAL.getGreen() * ratio + WHITE.getGreen() * (1 - ratio));
            int blue = (int) (PIVOTAL.getBlue() * ratio + WHITE.getBlue() * (1 - ratio));
            Color stepColor = new Color(red, green, blue);
            colors[i] = stepColor;
            // intermediate color
        }		
        return colors;
        
	}
	
	
	
	
    /**
     * @param rgbForHexConversion
     *           - comma separated rgb values in the format rrr,ggg, bbb e.g.
     *            "119,200,210"
     * @return equivalent hex in the format 0xXXXXXXXX e.g. 0x0077c8d2
     *
     *        If the converted hex value is not 8 <span id="IL_AD8" class="IL_AD">characters</span> long, pads the
     *         zeros in the front.
     */
    
	private static String convertRGBToHex(String rgbForHexConversion) {
        String hexValue = "";
        Pattern rgbPattern = Pattern.compile(commaSeparatedRGBPattern);
        Matcher rgbMatcher = rgbPattern.matcher(rgbForHexConversion);
 
        int red;
        int green;
        int blue;
        if (rgbMatcher.find()) {
            red = Integer.parseInt(rgbMatcher.group(1));
            green = Integer.parseInt(rgbMatcher.group(2));
            blue = Integer.parseInt(rgbMatcher.group(3));
            Color color = new Color(red, green, blue);
            hexValue = Integer.toHexString(color.getRGB() & 0x00ffffff);
            //int numberOfZeroesNeededForPadding = HEXLENGTH - hexValue.length();
            //String zeroPads = "";
            //for (int i = 0; i < numberOfZeroesNeededForPadding; i++) {
            //    zeroPads += "0";
           // }
            //hexValue = "0x" + zeroPads + hexValue;
        } else {
            System.out.println("Not a valid RGB String: "+rgbForHexConversion
                    + "\n>>>Please check your inut string.");
        }
 
         
        System.out.println();
        return hexValue;
    }	
	
    private static String convertHexToRGB(String hexForRGBConversion) {
        String rgbValue = "";
        Pattern hexPattern = Pattern.compile(hexaDecimalPattern);
        Matcher hexMatcher = hexPattern.matcher(hexForRGBConversion);
 
        if (hexMatcher.find()) {
            int hexInt = Integer.valueOf(hexForRGBConversion.substring(2), 16)
                    .intValue();
 
            int r = (hexInt & 0xFF0000) >> 16;
            int g = (hexInt & 0xFF00) >> 8;
            int b = (hexInt & 0xFF);
 
            rgbValue = r + "," + g + "," + b;
            System.out.println("Hex Value: " + hexForRGBConversion
                    + "\nEquivalent RGB Value: " + rgbValue);
        } else {
            System.out.println("Not a valid Hex String: " + hexForRGBConversion
                    + "\n>>>Please check your input string.");
        }
        System.out.println();
        return rgbValue;
 
    }	
	
    public static void main(String[] args) {
        String hexForRGBConversion = "0x00073f07";
        String whiteHexForRGBConversion = "0x00ffffff";
 
        String rgbToHex="85,123,85";
        
        /** Convert from HEX to RGB */
        //System.out.println("RGB: "+convertHexToRGB(hexForRGBConversion));
        //System.out.println("RGB: "+convertHexToRGB(whiteHexForRGBConversion));
         
        System.out.println("Hex: "+convertRGBToHex(rgbToHex));
    }	
    
    
}
