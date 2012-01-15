package org.saurabh.spaceshooter;

import java.util.ArrayList;
import java.util.Random;

import org.cocos2d.actions.base.CCRepeatForever;
import org.cocos2d.actions.instant.CCCallFuncN;
import org.cocos2d.actions.interval.CCFadeIn;
import org.cocos2d.actions.interval.CCFadeOut;
import org.cocos2d.actions.interval.CCMoveTo;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.events.CCTouchDispatcher;
import org.cocos2d.layers.CCColorLayer;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.menus.CCMenu;
import org.cocos2d.menus.CCMenuItemFont;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCLabel;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.opengl.CCBitmapFontAtlas;
import org.cocos2d.opengl.CCGLSurfaceView;
import org.cocos2d.particlesystem.CCParticleMeteor;
import org.cocos2d.particlesystem.CCParticleRain;
import org.cocos2d.particlesystem.CCParticleSystem;
import org.cocos2d.sound.SoundEngine;
import org.cocos2d.transitions.CCFlipXTransition;
import org.cocos2d.transitions.CCSlideInTTransition;
import org.cocos2d.transitions.CCTransitionScene;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGRect;
import org.cocos2d.types.CGSize;
import org.cocos2d.types.ccColor4B;

import com.saurabh.spaceshooter.R;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


public class SpaceShooter extends Activity {

    private CCGLSurfaceView mGLSurfaceView;
    private CGSize winSize;
    Context mContext;
    private final String TAG = "SpaceShooter";
    
    protected ArrayList<CCSprite> targetsList;    // the arraylist of falling red balls
	protected ArrayList<CCSprite> bulletsList;	  // the arraylist of shooting bullets
	protected int bulletsDestroyed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mGLSurfaceView = new CCGLSurfaceView(this);
        setContentView(mGLSurfaceView);
        
        targetsList = new ArrayList<CCSprite>();
		bulletsList = new ArrayList<CCSprite>();
		bulletsDestroyed = 0;
        
		

        applicationDidFinishLaunching(this, mGLSurfaceView);
        
        // Handle sound
		mContext = CCDirector.sharedDirector().getActivity();
	   	SoundEngine.sharedEngine().playSound(mContext, R.raw.background_music_aac, true);
    }

    /**
     * 
     * @author saurabh
     *  Layer1 is the home screen of the game.
     *  I've added the following types of nodes in this screen:
     *  1. Menu node (the continue text at the bottom)
     *  2. Meteor node (at the bottom right side of the screen)
     *  3. BG color node (red color)
     *  4. Font node (the text displayed at the center) 
     */
     class Layer1 extends CCLayer {

        CCParticleSystem meteor1 = CCParticleMeteor.node();
         
        public Layer1() {
            CCMenuItemFont item1 = CCMenuItemFont.item("Continue", this, "onPushScene");
            
            winSize = CCDirector.sharedDirector().displaySize();
            
            CCMenu menu = CCMenu.menu(item1);
            menu.setPosition(winSize.width/2,10);
            
            
            meteor1.setPosition(winSize.width, 0);
            addChild(meteor1,0);
            
            CCParticleSystem meteor2 = CCParticleMeteor.node();
            meteor2.setAngle(3.14f);
            //meteor2.setPosition(0, winSize.height);
            //addChild(meteor2,0);            
            
            CCLayer color = CCColorLayer.node(ccColor4B.ccc4(255, 0, 0, 255));
            CCBitmapFontAtlas font = CCBitmapFontAtlas.bitmapFontAtlas("Do You have enough","bitmapFontTest.fnt");
            font.setPosition(winSize.width/2, winSize.height/2);
            CCBitmapFontAtlas font1 = CCBitmapFontAtlas.bitmapFontAtlas("GUTS","bitmapFontTest.fnt");
            font1.setPosition(winSize.width/2,winSize.height/2-font.getAnchorPointInPixels().y-20);
            
            /*CCJumpBy jump = CCJumpBy.action(0.5f, CGPoint.zero(), 60, 1);
            CCRepeatForever jump_4ever = CCRepeatForever.action(jump);*/
            
            CCFadeOut fade_out = CCFadeOut.action(1);
            CCFadeIn fade_in = CCFadeIn.action(1);
            CCSequence seq = CCSequence.actions(fade_out, fade_in);
            CCRepeatForever fade_4ever = CCRepeatForever.action(seq);
            font1.runAction(fade_4ever);
            
            
            addChild(font1);
            addChild(font);
            
            addChild(color, -1);
            addChild(menu,1);

        }

        public void onPushScene(Object sender) {
            removeChild(meteor1, true);
        	CCScene s = CCScene.node();
            s.addChild(new Layer2(), 0);
            CCDirector.sharedDirector().replaceScene(CCFlipXTransition.transition(2.0f, s, CCTransitionScene.tOrientation.kOrientationLeftOver));
            /*CCScene scene = CCScene.node();
            scene.addChild(new Layer2(), 0);
            CCDirector.sharedDirector().pushScene(scene);*/
        }

        public void onPushSceneTran(Object sender) {
            CCScene scene = CCScene.node();
            scene.addChild(new Layer2(), 0);
            CCDirector.sharedDirector().pushScene(CCSlideInTTransition.transition(1, scene));
        }

        public void onQuit(Object sender) {
            CCDirector.sharedDirector().popScene();
        }
    }

    /** 
     * @author saurabh
     * This is the Game Screen. Add the following nodes in this Layer:
     * 1. Rain Particle System.
     * 2. SpaceShip Sprite
     * 3. Red Balls Sprite.
     */
    class Layer2 extends CCLayer {
    	
    	CCSprite hero;  // our hero space ship
    	
        public Layer2() {
        	
        	this.setIsTouchEnabled(true);
        	this.setIsAccelerometerEnabled(true);
        	
        	winSize = CCDirector.sharedDirector().displaySize();
        	
        	CCParticleSystem rain = CCParticleRain.node();
        	
        	hero = CCSprite.sprite("ship.png");
        	hero.setPosition(winSize.width/2,30);
        	
        	addChild(rain);
        	addChild(hero);
        	
    		this.schedule("gameLogic", 1.0f);
    		this.schedule("update");
     
        	
        	// couldnt implement infinitely scrolling parallex :( 
        	// Will come back to it \m/
        	
            /*CCSprite background = CCSprite.sprite("background_island.png",CGRect.make(0,0,2*winSize.width,2*winSize.height*3));
            CCSprite bg = CCSprite.sprite("background.png",CGRect.make(0,0,2*winSize.width,2*winSize.height*3));
            //background.setScale(4);
            
            //CGRect.make(0,0,240,320*1000);
            //CCTexParams params = new CCTexParams(GL10.GL_LINEAR,GL10.GL_LINEAR,GL10.GL_REPEAT,GL10.GL_REPEAT);
            //background.getTexture().setTexParameters(params);
            
            background.addChild(background);
            
            CCParallaxNode voidNode = CCParallaxNode.node();
 
            voidNode.addChild(background, -1, 0.4f, 0.1f, 0,0); */
          
        }
        
        public void gameLogic(float dt) {
        	addTarget();
        }
        
        public void update(float dt) {
        	ArrayList<CCSprite> bulletsToDelete = new ArrayList<CCSprite>();
    		
    		for (CCSprite bullet : bulletsList) {
    			
    			// make a CGRect object for detecting collision
    			CGRect bulletRect = CGRect.make(bullet.getPosition().x - (bullet.getContentSize().width / 2.0f),
    												bullet.getPosition().y - (bullet.getContentSize().height / 2.0f),
    												bullet.getContentSize().width,
    												bullet.getContentSize().height);
    			
    			ArrayList<CCSprite> targetsToDelete = new ArrayList<CCSprite>();
    			
    			for (CCSprite target : targetsList) {
    				CGRect targetRect = CGRect.make(target.getPosition().x - (target.getContentSize().width),
							target.getPosition().y - (target.getContentSize().height),
							target.getContentSize().width,
							target.getContentSize().height);
					
					if (CGRect.intersects(bulletRect, targetRect))  // collision detection
						targetsToDelete.add(target);
    			}
    			
    			for (CCSprite target : targetsToDelete) {
    				targetsList.remove(target);
    				removeChild(target, true);
    			}
    			if (targetsToDelete.size() > 0)
    				bulletsToDelete.add(bullet);
    		
    		}
    		for (CCSprite bullet : bulletsToDelete) {
    			bulletsList.remove(bullet);
    			removeChild(bullet, true);
    			
    			if (++bulletsDestroyed > 50)	// we can change the game play level here and can display  
    				bulletsDestroyed = 0;		// bulletsDestroyed to show the user his progress in the game
    		}
        }
        
        public void addTarget() {

    		Random rand = new Random();
    		CCSprite target = CCSprite.sprite("ball.png");
    		
    		// Determine where to spawn the target along the X axis
    		CGSize winSize = CCDirector.sharedDirector().displaySize();
    		int xPos = new Random().nextInt((int) (winSize.width-40))+20;
    		target.setPosition(xPos, winSize.height-10);
    		//target.setPosition(new Random().nextInt((int) (winSize.width-20))+20, winSize.height+40);
    		addChild(target);    		
    		
    		target.setTag(1);
    		targetsList.add(target);
    		
    		// Determine speed of the target
    		int minDuration = 3;
    		int maxDuration = 5;
    		int rangeDuration = maxDuration - minDuration;
    		int actualDuration = rand.nextInt(rangeDuration) + minDuration;
    		
    		CCMoveTo move = CCMoveTo.action(actualDuration,CGPoint.ccp(xPos, 0));
    		
    		// Create the actions
    		//CCMoveTo actionMove = CCMoveTo.action(actualDuration, CGPoint.ccp(-target.getContentSize().width / 2.0f, actualY));
    		CCCallFuncN actionMoveDone = CCCallFuncN.action(this, "spriteMoveFinished");
    		CCSequence actions = CCSequence.actions(move, actionMoveDone);
    		
    		target.runAction(actions);
    	
        }
        
        public void spriteMoveFinished(Object sender) {
        	CCSprite sprite = (CCSprite)sender;
            this.removeChild(sprite, true);
        }

        public void onGoBack(Object sender) {
            CCDirector.sharedDirector().popScene();
        }
         
        @Override
        public boolean ccTouchesBegan(MotionEvent event) {
        	
        	// shot sound
        	Context context = CCDirector.sharedDirector().getActivity();
    		SoundEngine.sharedEngine().playEffect(context, R.raw.shot);
        		        	
        	CCSprite bullet = CCSprite.sprite("magenta.png");
        	bullet.setPosition(hero.getPosition().x,50);
        	
        	addChild(bullet,-1);
    		bullet.setTag(2);
    		bulletsList.add(bullet);

        	CCMoveTo fire = CCMoveTo.action(0.5f,CGPoint.ccp(hero.getPosition().x, winSize.height+10));
     
            bullet.runAction(fire);
        	
        	return super.ccTouchesBegan(event);
        }
        
        @Override
        public void ccAccelerometerChanged(float accelX, float accelY,
        		float accelZ) {
        	super.ccAccelerometerChanged(accelX, accelY, accelZ);
        	
        	float x = winSize.width/2-(accelX*40);
        	x = Math.round(x);
        	
        	hero.setPosition(x, 30);
        }       
        
    }

    // CLASS IMPLEMENTATIONS
    public void applicationDidFinishLaunching(Context context, View view) {

        // attach the OpenGL view to a window
        CCDirector.sharedDirector().attachInView(view);

        // set landscape mode
        CCDirector.sharedDirector().setLandscape(false);

        // show FPS
        CCDirector.sharedDirector().setDisplayFPS(false);

        // frames per second
        CCDirector.sharedDirector().setAnimationInterval(1.0f / 60);

        CCScene scene = CCScene.node();
        scene.addChild(new Layer1(), 0);

        // Make the Scene active
        CCDirector.sharedDirector().runWithScene(scene);

    }
    
    public void onPause() {
    	super.onPause();
    	CCDirector.sharedDirector().onPause();
    	SoundEngine.sharedEngine().pauseSound();
    }

    public void onResume() {
    	super.onResume();
        CCDirector.sharedDirector().onResume();
        SoundEngine.sharedEngine().resumeSound();
    }

    public void onDestroy() {
    	SoundEngine.sharedEngine().realesAllEffects();
    	CCDirector.sharedDirector().end();
    	super.onDestroy();
    }
    
    @Override
    protected void onStop() {
    	SoundEngine.sharedEngine().realesAllEffects();
    	super.onStop();
    }
}
