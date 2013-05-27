package com.lewa.launcher;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.animation.Transformation;

public class LWEffectFactory {

	private Workspace mWorkspace;
	private Camera mCamera = new Camera();

	public LWEffectFactory(Workspace workspace) {
		this.mWorkspace = workspace;
	}

	public boolean doEffectTransformation(View child, Transformation t, int type) {
		boolean flag = false;
		switch (type) {
		case 0:
			break;
		case 1:
			break;
		case 2:
			flag = doCube(child, t);
			break;
		case 3:
			flag = doRotate(child, t);
			break;
		case 4:
			flag = doStact(child, t);
			break;
		default:
			break;
		}
		return flag;
	}

	public float getCurrentScrollRadio(View view) {
		float childWidht = view.getMeasuredWidth();
		float workspaceWidth = mWorkspace.getMeasuredWidth();
		float scrollX = mWorkspace.getScrollX();
		// System.out.println("view.getLeft() = "+view.getLeft()
		// +" xxx = "+mWorkspace.getScrollX());
		float childLeft = view.getLeft();
		if(scrollX > (mWorkspace.getChildCount()-1)*childWidht){
			if(childLeft == (mWorkspace.getChildCount()-2)*childWidht){
				return 0;
			}
			childLeft += childWidht;
			if(childLeft > (mWorkspace.getChildCount()-1)*childWidht)
				childLeft = 0;
			scrollX = scrollX - (mWorkspace.getChildCount()-1)*childWidht;
		} else if(scrollX < 0){
			if(childLeft == (mWorkspace.getChildCount()-1)*childWidht){
				scrollX = childLeft + scrollX;
				childLeft -= childWidht;
			}
		}
		float offsetX = scrollX + workspaceWidth / 2F - childLeft - childWidht
				/ 2F;
		float widthed = workspaceWidth / 2F + childWidht / 2F;
		return Math.max(offsetX / widthed, -1F);
	}

	private boolean doCube(View child, Transformation t) {
		float currentScroll = getCurrentScrollRadio(child);
		float childWidht = child.getMeasuredWidth();
		float childHeight = child.getMeasuredHeight();

		Matrix matrix = t.getMatrix();
		mCamera.save();
		mCamera.rotateY(-45F * currentScroll);
		mCamera.getMatrix(matrix);
		mCamera.restore();

		if (currentScroll < 0F) {
			matrix.preTranslate(0F, -childHeight / 2F);
			matrix.postTranslate(0F, childHeight / 2F);
		} else {
			matrix.preTranslate(-childWidht, -childHeight / 2F);
			matrix.postTranslate(childWidht, childHeight / 2F);
		}
		t.setTransformationType(Transformation.TYPE_MATRIX);
		return true;
	}

	private boolean doRotate(View child, Transformation transformation) {
		float currentScroll = getCurrentScrollRadio(child);
		float childWidth = child.getMeasuredWidth();
		float childHeight = child.getMeasuredHeight();

		Matrix matrix = transformation.getMatrix();
		matrix.setRotate(-currentScroll * 45F, childWidth / 2F, childHeight);
		transformation.setTransformationType(Transformation.TYPE_MATRIX);
		return true;
	}

	private boolean doStact(View child, Transformation transformation) {
		float currentScroll = getCurrentScrollRadio(child);
		float childWidht = child.getMeasuredWidth();
		float childHeight = child.getMeasuredHeight();

		if (currentScroll > 0F) {
			Matrix matrix = transformation.getMatrix();
			transformation.setAlpha(1F - currentScroll);
			float scale = 0.4F * (1F - currentScroll) + 0.6F;
			matrix.setScale(scale, scale);
			// matrix.postTranslate((1F - scale) * childWidht * 3F, (1F - scale)
			// * childHeight * 0.5F);
			matrix.postTranslate((1F - scale) * childWidht * 1F, (1F - scale)
					* childHeight * 0.5F);
			transformation.setTransformationType(Transformation.TYPE_BOTH);
			return true;
		} else {
			return false;
		}
	}
}
