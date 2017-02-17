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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DeviceDAO {
    
    private static final DeviceDAO INSTANCE = new DeviceDAO();

    public static DeviceDAO getInstance() {
        return INSTANCE;
    }
    
    
    private final Map <String,Device> deviceLookup = new HashMap<>();
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();
    
    private DeviceDAO() {}
    
    public void addDevice( Device d ) {
        writeLock.lock();
        try {
            if ( deviceLookup.containsKey(d.getId()) ) {
                throw new IllegalArgumentException("Device with ID = \"" + d.getId() + "\" already exists!");
            }
            Device copy = new Device(d);
            deviceLookup.put( copy.getId(), copy );
        } finally {
            writeLock.unlock();
        }
    }
    
    public void updateDevice( Device d ) {
        writeLock.lock();
        try {
            if ( !deviceLookup.containsKey(d.getId()) ) {
                throw new IllegalArgumentException("Device with ID = \"" + d.getId() + "\" cannot be updated because it does not exists!");
            }
            Device copy = new Device(d);
            deviceLookup.put( copy.getId(), copy );
        } finally {
            writeLock.unlock();
        }
    }
    
    public boolean containsDevice( String deviceId ) {
        readLock.lock();
        try {
            return deviceLookup.containsKey( deviceId );
        } finally {
            readLock.unlock();
        }
    }
    
    public Device getDevice( String deviceId ) {
        readLock.lock();
        try {
            Device d = deviceLookup.get( deviceId );
            if ( d != null ) {
                return new Device(d);
            } else {
                return null;
            }                
        } finally {
            readLock.unlock();
        }
    }
    
}
