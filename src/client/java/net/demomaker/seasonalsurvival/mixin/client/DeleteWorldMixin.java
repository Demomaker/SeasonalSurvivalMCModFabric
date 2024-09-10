package net.demomaker.seasonalsurvival.mixin.client;

import net.demomaker.seasonalsurvival.ClientModStateManager;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelStorage.Session.class)
public class DeleteWorldMixin {
  @Shadow
  @Final
  private String directoryName;
  @Inject(method = "deleteSessionLock", at = @At("TAIL"))
  public void deleteWorld(CallbackInfo ci) throws Exception {
    ClientModStateManager.deleteWorldByName(directoryName);
  }
}
