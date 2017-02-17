/*

MIT License

Copyright (c) 2017 Grzegorz Hołdys

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

*/

package sbl.devices;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Device {

    private String id;
    private String displayName;
    private String timeZone;
    private int dailyRation; // [g]

    public Device() {
    }
    
    public Device( Device src ) {
        this.id = src.id;
        this.displayName = src.displayName;
        this.timeZone = src.timeZone;
        this.dailyRation = src.dailyRation;
    }
    
    public Device( String id, String displayName, String timeZone, int dailyRation ) {
        this.id = id;
        this.displayName = displayName;
        this.timeZone = timeZone;
        this.dailyRation = dailyRation;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timezone) {
        this.timeZone = timezone;
    }

    public int getDailyRation() {
        return dailyRation;
    }

    public void setDailyRation(int dailyRation) {
        this.dailyRation = dailyRation;
    }

    @Override
    public String toString() {
        return displayName + "; id = " + id + "; daily ration = " + dailyRation + "; time zone = " + timeZone;
    }

    
}
