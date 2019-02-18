/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ducpad;

/**
 *
 * @author Duc Pham Le
 */
public class KeyBoard {

    public static boolean[] keyPressed = new boolean[128];

    public static boolean isPressed(int key) {
        return keyPressed[key];
    }

    public static void keyInit() {
        for (int i = 0; i < keyPressed.length; i++) {
            keyPressed[i] = false;
        }
    }
}
