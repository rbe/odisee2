package eu.artofcoding.grails.helper

class WallTime {

    long startWallTime
    long stopWallTime
    
    def start() {
        startWallTime = System.currentTimeMillis()
    }
    
    def stop() {
        stopWallTime = System.currentTimeMillis()
    }
    
    long diff() {
        stopWallTime - startWallTime
    }
    
}
