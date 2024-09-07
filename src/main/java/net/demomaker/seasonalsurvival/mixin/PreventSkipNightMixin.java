package net.demomaker.seasonalsurvival.mixin;

import java.util.List;
import net.demomaker.seasonalsurvival.SeasonalSurvival;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.SleepManager;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SleepManager.class)
public class PreventSkipNightMixin {
  @Inject(method = "canResetTime", at = @At("HEAD"), cancellable = true)
  private void alertResetIsNotPossible(int percentage, List<ServerPlayerEntity> players, CallbackInfoReturnable<Boolean> cir) {
    if(SeasonalSurvival.isWinter) {
      players.forEach(player -> player.sendMessage(Text.translatable("seasonalsurvival.winterNoSleep")));
      cir.setReturnValue(false);
    }
  }
}
