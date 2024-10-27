package net.demomaker.seasonalsurvival.mixin;

import net.demomaker.seasonalsurvival.ServerWorldSettingResolver;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
public class SnowMixin {
  @Inject(method = "getTemperature", at = @At("HEAD"), cancellable = true)
  private void getTemperature(BlockPos blockPos, int seaLevel, CallbackInfoReturnable<Float> cir) {
    winterTemperature(blockPos, seaLevel, cir);
  }

  @Inject(method = "computeTemperature", at = @At("HEAD"), cancellable = true)
  private void computeTemperature(BlockPos blockPos, int seaLevel, CallbackInfoReturnable<Float> cir) {
    winterTemperature(blockPos, seaLevel, cir);
  }

  private void winterTemperature(BlockPos blockPos, int seaLevel, CallbackInfoReturnable<Float> cir) {
    if(ServerWorldSettingResolver.isWinter()) {
      cir.setReturnValue(0.01F);
    }
  }

  @Mixin(targets = "net.minecraft.world.biome.Biome$Weather")
  public static class WeatherMixin {
    @Inject(method = "downfall", at = @At("HEAD"), cancellable = true)
    private void showDownfall(CallbackInfoReturnable<Float> cir) {
      if(ServerWorldSettingResolver.isWinter()) {
        cir.setReturnValue(1.0f);
      }
    }

    @Inject(method = "hasPrecipitation", at = @At("HEAD"), cancellable = true)
    private void injectHasPrecipitation(CallbackInfoReturnable<Boolean> cir) {
      // Add your condition for temporarily allowing rain particles
      if (ServerWorldSettingResolver.isWinter()) {
        cir.setReturnValue(true); // Override the result to true to show precipitation
      }
    }
  }
}
