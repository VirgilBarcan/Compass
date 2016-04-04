package com.barcan.virgil.compass;

/**
 * Created by virgil on 05.04.2016.
 */
public class LowPassFilter {

    private static final float ALPHA = 0.2f;


    /**
     * https://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
     * @param input the input
     * @param output the output
     * @return the smoothed values
     */
    public static float[] filter(float[] input, float[] output) {
        if (output == null) {
            return input;
        }

        for (int i = 0; i < input.length; ++i) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }

        return output;
    }
}
