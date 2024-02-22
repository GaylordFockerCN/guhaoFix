package com.guhao.skills;

import net.minecraft.server.level.ServerPlayer;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPPlayAnimation;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataManager;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.UUID;

public class GUHAOPASSIVE extends Skill{
    public static final SkillDataManager.SkillDataKey<Boolean> SHEATH = SkillDataManager.SkillDataKey.createDataKey(SkillDataManager.ValueType.BOOLEAN);
    private static final UUID EVENT_UUID = UUID.fromString("a416c93a-42cb-11eb-b378-0242ac130002");

    public GUHAOPASSIVE(Skill.Builder<? extends Skill> builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);
        container.getDataManager().registerData(SHEATH);
        container.getExecuter().getEventListener().addEventListener(PlayerEventListener.EventType.ACTION_EVENT_SERVER, EVENT_UUID, (event) -> {
            container.getSkill().setConsumptionSynchronize(event.getPlayerPatch(), 0.0F);
            container.getSkill().setStackSynchronize(event.getPlayerPatch(), 0);
        });

        container.getExecuter().getEventListener().addEventListener(PlayerEventListener.EventType.SERVER_ITEM_USE_EVENT, EVENT_UUID, (event) -> {
            this.onReset(container);
        });
    }

    @Override
    public void onRemoved(SkillContainer container) {
        container.getExecuter().getEventListener().removeListener(PlayerEventListener.EventType.ACTION_EVENT_SERVER, EVENT_UUID);
        container.getExecuter().getEventListener().removeListener(PlayerEventListener.EventType.SERVER_ITEM_USE_EVENT, EVENT_UUID);
    }

    @Override
    public void onReset(SkillContainer container) {
        PlayerPatch<?> executer = container.getExecuter();

        if (!executer.isLogicalClient()) {
            if (container.getDataManager().getDataValue(SHEATH)) {
                ServerPlayerPatch playerpatch = (ServerPlayerPatch)executer;
                container.getDataManager().setDataSync(SHEATH, false, playerpatch.getOriginal());
                playerpatch.modifyLivingMotionByCurrentItem();
                container.getSkill().setConsumptionSynchronize(playerpatch, 0);
            }
        }
    }

    @Override
    public void setConsumption(SkillContainer container, float value) {
        PlayerPatch<?> executer = container.getExecuter();

        if (!executer.isLogicalClient()) {
            if (container.getMaxResource() < value) {
                ServerPlayer serverPlayer = (ServerPlayer) executer.getOriginal();
                container.getDataManager().setDataSync(SHEATH, true, serverPlayer);
                ((ServerPlayerPatch)container.getExecuter()).modifyLivingMotionByCurrentItem();
                SPPlayAnimation msg3 = new SPPlayAnimation(Animations.BIPED_UCHIGATANA_SCRAP, serverPlayer.getId(), 0.0F);
                EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(msg3, serverPlayer);
            }
        }

        super.setConsumption(container, value);
    }

    @Override
    public boolean shouldDeactivateAutomatically(PlayerPatch<?> executer) {
        return true;
    }

    @Override
    public float getCooldownRegenPerSecond(PlayerPatch<?> player) {
        return player.getOriginal().isUsingItem() ? 0.0F : 1.0F;
    }
}
