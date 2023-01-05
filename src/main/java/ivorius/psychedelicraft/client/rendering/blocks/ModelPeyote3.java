/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

// Date: 29.07.2013 22:54:33
// Template version 1.1
// Java generated by Techne
// Keep in mind that you still need to fill in some blanks
// - ZeuX

package ivorius.psychedelicraft.client.rendering.blocks;
/*
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

@Deprecated
public class ModelPeyote3 extends ModelBase
{
    //fields
    ModelRenderer peyote1;
    ModelRenderer peyote2;
    ModelRenderer peyote3;
    ModelRenderer peyote4;
    ModelRenderer peyote5;
    ModelRenderer peyote6;
    ModelRenderer peyote7;
    ModelRenderer peyote8;
    ModelRenderer peyote9;
    ModelRenderer peyote10;
    ModelRenderer flower1;
    ModelRenderer flower2;

    public ModelPeyote3()
    {
        textureWidth = 64;
        textureHeight = 32;

        peyote1 = new ModelRenderer(this, 0, 0);
        peyote1.addBox(-2F, -3F, -3F, 4, 4, 4);
        peyote1.setRotationPoint(-1F, 24F, -1F);
        peyote1.setTextureSize(64, 32);
        peyote1.mirror = true;
        setRotation(peyote1, 0.1115358F, 0.1858931F, -0.1487144F);
        peyote2 = new ModelRenderer(this, 0, 9);
        peyote2.addBox(0F, -2.5F, -3F, 4, 3, 4);
        peyote2.setRotationPoint(0.5F, 24F, -1F);
        peyote2.setTextureSize(64, 32);
        peyote2.mirror = true;
        setRotation(peyote2, 0.1858931F, -0.2602503F, 0.1115358F);
        peyote3 = new ModelRenderer(this, 0, 17);
        peyote3.addBox(-2F, -2.5F, 0.5F, 4, 3, 4);
        peyote3.setRotationPoint(0F, 24F, 0F);
        peyote3.setTextureSize(64, 32);
        peyote3.mirror = true;
        setRotation(peyote3, -0.1487144F, 0.3717861F, 0F);
        peyote4 = new ModelRenderer(this, 0, 25);
        peyote4.addBox(-3.5F, -2F, 0F, 3, 3, 3);
        peyote4.setRotationPoint(0F, 24F, 0F);
        peyote4.setTextureSize(64, 32);
        peyote4.mirror = true;
        setRotation(peyote4, -0.1487144F, -0.1335332F, -0.1115358F);
        peyote5 = new ModelRenderer(this, 17, 10);
        peyote5.addBox(-1F, -2F, 0F, 3, 3, 3);
        peyote5.setRotationPoint(3F, 24F, 0F);
        peyote5.setTextureSize(64, 32);
        peyote5.mirror = true;
        setRotation(peyote5, -0.2602503F, 0.6320364F, 0F);
        peyote6 = new ModelRenderer(this, 17, 0);
        peyote6.addBox(0F, -1.5F, 1F, 2, 2, 2);
        peyote6.setRotationPoint(0F, 24F, 2F);
        peyote6.setTextureSize(64, 32);
        peyote6.mirror = true;
        setRotation(peyote6, 0F, -1.375609F, 0.2974289F);
        peyote7 = new ModelRenderer(this, 17, 5);
        peyote7.addBox(-2F, -1F, -1F, 2, 2, 2);
        peyote7.setRotationPoint(-3F, 24F, -1F);
        peyote7.setTextureSize(64, 32);
        peyote7.mirror = true;
        setRotation(peyote7, 0.0743572F, 0.2230717F, -0.2230717F);
        peyote8 = new ModelRenderer(this, 17, 16);
        peyote8.addBox(0F, -1.5F, -1F, 2, 2, 2);
        peyote8.setRotationPoint(0F, 24F, -5F);
        peyote8.setTextureSize(64, 32);
        peyote8.mirror = true;
        setRotation(peyote8, 0.1487144F, -0.2602503F, 0F);
        peyote9 = new ModelRenderer(this, 17, 21);
        peyote9.addBox(-1F, -1F, 0F, 1, 1, 1);
        peyote9.setRotationPoint(-2F, 24F, -5F);
        peyote9.setTextureSize(64, 32);
        peyote9.mirror = true;
        setRotation(peyote9, 0F, -0.2602503F, -0.2974289F);
        peyote10 = new ModelRenderer(this, 17, 24);
        peyote10.addBox(0F, -1F, 0F, 1, 1, 1);
        peyote10.setRotationPoint(0F, 24F, 5F);
        peyote10.setTextureSize(64, 32);
        peyote10.mirror = true;
        setRotation(peyote10, -0.1487144F, 0.1487144F, 0F);
        flower1 = new ModelRenderer(this, 26, 0);
        flower1.addBox(0F, -5F, -2.5F, 0, 2, 3);
        flower1.setRotationPoint(-1F, 24F, -1F);
        flower1.setTextureSize(64, 32);
        flower1.mirror = true;
        setRotation(flower1, 0.111544F, 0.185895F, -0.1487195F);
        flower2 = new ModelRenderer(this, 26, 0);
        flower2.addBox(-1.5F, -5F, -1F, 3, 2, 0);
        flower2.setRotationPoint(-1F, 24F, -1F);
        flower2.setTextureSize(64, 32);
        flower2.mirror = true;
        setRotation(flower2, 0.111544F, 0.185895F, -0.1487195F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        peyote1.render(f5);
        peyote2.render(f5);
        peyote3.render(f5);
        peyote4.render(f5);
        peyote5.render(f5);
        peyote6.render(f5);
        peyote7.render(f5);
        peyote8.render(f5);
        peyote9.render(f5);
        peyote10.render(f5);
        flower1.render(f5);
        flower2.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
*/