package de.ellpeck.sparks.mod.entity.spark;

import de.ellpeck.sparks.api.iface.ISparkInteractor;
import de.ellpeck.sparks.mod.Sparks;
import de.ellpeck.sparks.mod.entity.spark.base.EntityTravellingSparkBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityPotentialSpark extends EntityTravellingSparkBase{

    private static final DataParameter<Integer> POTENTIAL_AMOUNT = EntityDataManager.createKey(EntityPotentialSpark.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> POTENTIAL_MAX = EntityDataManager.createKey(EntityPotentialSpark.class, DataSerializers.VARINT);

    public EntityPotentialSpark(World world){
        super(world);
    }

    public EntityPotentialSpark(World world, double x, double y, double z, EnumFacing facing, double motion, int amount, int maxAmount){
        super(world, x, y, z, facing, motion);

        this.setPotential(amount);
        this.setMaxPotential(maxAmount);
    }

    @Override
    public void onUpdate(){
        super.onUpdate();

        if(!this.world.isRemote){
            if(this.getPotential() <= 0){
                this.kill();
            }
        }
        else{
            float percentage = (float)this.getPotential()/(float)this.getMaxPotential();
            int maxAge = (int)(80F*percentage+20F);
            Sparks.proxy.spawnMagicParticle(this.world, (float)(this.prevPosX+(this.posX-this.prevPosX)*5F), (float)(this.prevPosY+(this.posY-this.prevPosY)*5F), (float)(this.prevPosZ+(this.posZ-this.prevPosZ)*5F), 0.0125F*(this.rand.nextFloat()-0.5F), 0.0125F*(this.rand.nextFloat()-0.5F), 0.0125F*(this.rand.nextFloat()-0.5F), this.getColor(), 3F, maxAge, 0F, false);
        }
    }

    @Override
    protected void entityInit(){
        super.entityInit();

        this.dataManager.register(POTENTIAL_AMOUNT, 0);
        this.dataManager.register(POTENTIAL_MAX, 0);
    }

    public void setPotential(int potential){
        this.dataManager.set(POTENTIAL_AMOUNT, potential);
    }

    public int getPotential(){
        return this.dataManager.get(POTENTIAL_AMOUNT);
    }

    public void setMaxPotential(int max){
        this.dataManager.set(POTENTIAL_MAX, max);
    }

    public int getMaxPotential(){
        return this.dataManager.get(POTENTIAL_MAX);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound){
        super.writeEntityToNBT(compound);

        compound.setInteger("Potential", this.getPotential());
        compound.setInteger("MaxPotential", this.getMaxPotential());
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound){
        super.readEntityFromNBT(compound);

        this.setPotential(compound.getInteger("Potential"));
        this.setMaxPotential(compound.getInteger("MaxPotential"));
    }

    @Override
    protected void onInteraction(ISparkInteractor block, BlockPos pos, IBlockState state, EnumActionResult result){
        if(result == EnumActionResult.SUCCESS){
            this.setPotential(this.getPotential()-5);
        }
    }

    @Override
    public boolean split(ISparkInteractor splitter, BlockPos pos, IBlockState state, EnumFacing firstDir, EnumFacing secondDir){
        for(int i = 0; i < 2; i++){
            EntityPotentialSpark spark = new EntityPotentialSpark(this.world, this.posX, this.posY, this.posZ, i == 0 ? firstDir : secondDir, this.motion, this.getPotential()/2, this.getMaxPotential());
            spark.setColor(this.getColor());
            spark.setLastInteractor(pos);
            this.world.spawnEntity(spark);
        }

        return true;
    }
}
