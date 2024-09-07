package net.demomaker.seasonalsurvival.mixin.client;

import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import net.demomaker.seasonalsurvival.ICreateWorldScreenMixin;
import net.demomaker.seasonalsurvival.ModObjects;
import net.demomaker.seasonalsurvival.ClientModStateManager;
import net.demomaker.seasonalsurvival.SeasonalSurvival;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.tab.GridScreenTab;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.text.Text;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.LevelProperties.SpecialProperty;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorage.Session;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin extends Screen implements ICreateWorldScreenMixin {
  @Unique
  private static final Text PREPARING_TEXT = Text.translatable("createWorld.preparing");

  @Shadow
  @Final
  private WorldCreator worldCreator;

  protected CreateWorldScreenMixin(Text title) {
    super(title);
  }

  @Inject(method = "startServer", at =@At("HEAD"), cancellable = true)
  private void saveWorldCreationSettings(LevelProperties.SpecialProperty specialProperty, CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries, Lifecycle lifecycle, CallbackInfo info) {
    showMessage(this.client, PREPARING_TEXT);
    Optional<LevelStorage.Session> optional = this.createSession();
    if (!optional.isEmpty()) {
      this.clearDataPackTempDir();
      boolean bl = specialProperty == SpecialProperty.DEBUG;
      GeneratorOptionsHolder generatorOptionsHolder = this.worldCreator.getGeneratorOptionsHolder();
      LevelInfo levelInfo = this.createLevelInfo(bl);
      SaveProperties saveProperties = new LevelProperties(levelInfo, generatorOptionsHolder.generatorOptions(), specialProperty, lifecycle);
      ClientModStateManager.saveWorldSettings(saveProperties.getLevelName(), ModObjects.seasonalSurvivalWorldSettings);
      this.client.createIntegratedServerLoader().startNewWorld((LevelStorage.Session)optional.get(), generatorOptionsHolder.dataPackContents(), combinedDynamicRegistries, saveProperties);
    }

    info.cancel();
  }

  @Shadow
  private LevelInfo createLevelInfo(boolean bl) { return null; }

  @Shadow
  private static void showMessage(MinecraftClient client, Text preparingText) {}

  @Shadow
  private Optional<Session> createSession() { return null; }

  @Shadow
  private void clearDataPackTempDir() {}

  @Override
  public TextRenderer getTextRenderer() {
    return this.textRenderer;
  }

  @Mixin(targets = "net.minecraft.client.gui.screen.world.CreateWorldScreen$GameTab")
  public static class GameTabMixin extends GridScreenTab {

    protected GameTabMixin(Text title) {
      super(title);
    }

    @Inject(method = "<init>(Lnet/minecraft/client/gui/screen/world/CreateWorldScreen;)V", at = @At("TAIL"))
    public void GameTabConstructor(CreateWorldScreen this$0, CallbackInfo ci) {
      CheckboxWidget customCheckbox = CheckboxWidget
          .builder(Text.translatable("seasonalsurvival.checkbox"), ((ICreateWorldScreenMixin) this$0).getTextRenderer())
          .pos(0, 0)
          .maxWidth(200)
          .callback((button, isChecked) -> {
            ModObjects.seasonalSurvivalWorldSettings.useSeasonalSurvival = isChecked;
          })
          .build();
      this.grid.add(customCheckbox, 4, 0);
    }
  }
}
