package net.demomaker.seasonalsurvival.mixin.client;

import net.demomaker.seasonalsurvival.SeasonalSurvivalClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(Biome.class)
public class ClientSeeSnowMixin {
    @Inject(method = "getTemperature", at = @At("HEAD"), cancellable = true)
    private void snowOnly(BlockPos blockPos, CallbackInfoReturnable<Float> cir) {
        if(SeasonalSurvivalClient.isWinter()) {
            cir.setReturnValue(0.01F);
        }
    }

    @Mixin(targets = "net.minecraft.world.biome.Biome$Weather")
    public static class ClientWeatherMixin {
        @Inject(method = "downfall", at = @At("HEAD"), cancellable = true)
        private void showDownfall(CallbackInfoReturnable<Float> cir) {
            if(SeasonalSurvivalClient.isWinter()) {
                cir.setReturnValue(1.0f);
            }
        }

        @Inject(method = "hasPrecipitation", at = @At("HEAD"), cancellable = true)
        private void injectHasPrecipitation(CallbackInfoReturnable<Boolean> cir) {
            // Add your condition for temporarily allowing rain particles
            if (SeasonalSurvivalClient.isWinter()) {
                cir.setReturnValue(true); // Override the result to true to show precipitation
            }
        }
    }
}
