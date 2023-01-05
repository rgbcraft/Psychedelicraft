package ivorius.psychedelicraft.util;

import net.minecraft.util.math.MathHelper;

public interface MathUtils {
    static float nearValue(float from, float to, float delta, float plusSpeed) {
        return approach(MathHelper.lerp(delta, from, to), to, plusSpeed);
    }

    static float approach(float value, float target, float adjustmentRate) {
        if (value > target) {
            return Math.max(value - adjustmentRate, target);
        }

        if (value < target) {
            return Math.min(value + adjustmentRate, target);
        }

        return value;
    }

    static double nearValue(double from, double to, double delta, double plusSpeed) {
        return approach(MathHelper.lerp(delta, from, to), to, plusSpeed);
    }

    static double approach(double value, double target, double adjustmentRate) {
        if (value > target) {
            return Math.max(value - adjustmentRate, target);
        }

        if (value < target) {
            return Math.min(value + adjustmentRate, target);
        }

        return value;
    }


    static float[] unpackRgb(int left) {
        return new float[] {r(left), g(left), b(left)};
    }

    static float[] unpackArgb(int left) {
        return new float[] {a(left), r(left), g(left), b(left)};
    }

    static float a(int left) {
        return (left >> 24 & 255) / 255F;
    }

    static float r(int left) {
        return (left >> 16 & 255) / 255F;
    }

    static float g(int left) {
        return (left >> 8 & 255) / 255F;
    }

    static float b(int left) {
        return (left & 255) / 255F;
    }

    static int mixColors(int left, int right, float progress) {
        return packArgb(
                MathHelper.lerp(a(left), a(right), progress),
                MathHelper.lerp(r(left), r(right), progress),
                MathHelper.lerp(g(left), g(right), progress),
                MathHelper.lerp(b(left), b(right), progress)
        );
    }

    static int packArgb(float a, float r, float g, float b) {
        return packArgb(
                MathHelper.floor(a * 255 + 0.5F),
                MathHelper.floor(r * 255 + 0.5F),
                MathHelper.floor(g * 255 + 0.5F),
                MathHelper.floor(b * 255 + 0.5F)
        );
    }

    static int packArgb(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    static float mixEaseInOut(float v1, float v2, float delta) {
        return cubicMix(v1, v1, v2, v2, delta);
    }

    static double mixEaseInOut(double v1, double v2, double delta) {
        return cubicMix(v1, v1, v2, v2, delta);
    }

    static double easeZeroToOne(double delta) {
        return cubicMix(0, 0, 1, 1, MathHelper.clamp(delta, 0, 1));
    }

    static float easeZeroToOne(float delta) {
        return cubicMix(0, 0, 1, 1, MathHelper.clamp(delta, 0, 1));
    }

    static float cubicMix(float v1, float v2, float v3, float v4, float delta) {
        return (float)MathHelper.lerp3(delta, delta, delta, v1, v2, v2, v3, v2, v3, v3, v4);
    }

    static double cubicMix(double v1, double v2, double v3, double v4, double delta) {
        return MathHelper.lerp3(delta, delta, delta, v1, v2, v2, v3, v2, v3, v3, v4);
    }
}
