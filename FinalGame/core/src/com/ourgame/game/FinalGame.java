package com.ourgame.game;

import java.util.Iterator;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

public class FinalGame extends ApplicationAdapter {
	private Texture enemy1;
	private Texture bucketImage;
	private Sound dropSound;
	private Music rainMusic;
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private Rectangle bucket, shipR;
	private Array<Rectangle> raindrops;
	private long lastDropTime;
	int x = 300, y = 0;
	float elapsed_time;
	private static final float FRAME_TIME = 1 / 15F;
	private Animation<AtlasRegion> spacequiet, spaceright,spaceleft;
	Animation<TextureRegion> walkAnimation;
	Texture walkSheet;
	float stateTime;
	@Override
	public void create () {
	      // load the images for the droplet and the bucket, 64x64 pixels each
		  enemy1 = new Texture(Gdx.files.internal("enemy.png"));
	      bucketImage = new Texture(Gdx.files.internal("quiet.png"));
	      
	        
	      TextureAtlas charset = new TextureAtlas(Gdx.files.internal("spacequiet.atlas"));
	      spacequiet = new Animation<>(FRAME_TIME, charset.findRegions("spacequiet"));
	      spacequiet.setFrameDuration(FRAME_TIME);
	      walkSheet = new Texture(Gdx.files.internal("spacequiet.png"));
	      
	      TextureAtlas charset1 = new TextureAtlas(Gdx.files.internal("spaceshipright.atlas"));
	      spaceright = new Animation<>(FRAME_TIME, charset1.findRegions("space"));
	      spaceright.setFrameDuration(FRAME_TIME);
	      walkSheet = new Texture(Gdx.files.internal("spaceshipright.png"));
	      
	      TextureAtlas charset2 = new TextureAtlas(Gdx.files.internal("spaceshipleft.atlas"));
	      spaceleft = new Animation<>(FRAME_TIME, charset2.findRegions("spaceL"));
	      spaceleft.setFrameDuration(FRAME_TIME);
	      walkSheet = new Texture(Gdx.files.internal("spaceshipleft.png"));

	      // load the drop sound effect and the rain background "music"
	      //dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
	      //rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

	      // start the playback of the background music immediately
	      //rainMusic.setLooping(true);
	      //rainMusic.play();

	      // create the camera and the SpriteBatch
	      camera = new OrthographicCamera();
	      camera.setToOrtho(false, 800, 480);
	      batch = new SpriteBatch();

	      // create a Rectangle to logically represent the bucket
	      shipR = new Rectangle();
	      shipR.x = 800 / 2 - 64 / 2; // center the bucket horizontally
	      shipR.y = 20; // bottom left corner of the bucket is 20 pixels above the bottom screen edge
	      shipR.width = 64;
	      shipR.height = 64;

	      // create the raindrops array and spawn the first raindrop
	      raindrops = new Array<Rectangle>();
	      spawnRaindrop();
	}

	   private void spawnRaindrop() {
		      Rectangle raindrop = new Rectangle();
		      raindrop.x = MathUtils.random(0, 800-64);
		      raindrop.y = 480;
		      raindrop.width = 64;
		      raindrop.height = 64;
		      raindrops.add(raindrop);
		      lastDropTime = TimeUtils.nanoTime();
   }
	   
	   
	@Override
	public void render () {
	      // clear the screen with a dark blue color. The
	      // arguments to clear are the red, green
	      // blue and alpha component in the range [0,1]
	      // of the color to be used to clear the screen.
	      ScreenUtils.clear(0, 0, 0.2f, 1);
	      elapsed_time += Gdx.graphics.getDeltaTime();
	      TextureRegion currentFrame2 = spacequiet.getKeyFrame(elapsed_time, true);
			if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
				currentFrame2 = spaceright.getKeyFrame(elapsed_time, true);
			if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
				currentFrame2 = spaceleft.getKeyFrame(elapsed_time, true);
	      super.render(); // important!
	      
	      // tell the camera to update its matrices.
	      camera.update();

	      // tell the SpriteBatch to render in the
	      // coordinate system specified by the camera.
	      batch.setProjectionMatrix(camera.combined);

	      // begin a new batch and draw the bucket and
	      // all drops
	      batch.begin();
	      batch.draw(currentFrame2, shipR.x, shipR.y);
	      for(Rectangle raindrop: raindrops) {
	         batch.draw(enemy1, raindrop.x, raindrop.y);
	      }
	      batch.end();

	      // process user input
	      if(Gdx.input.isTouched()) {
	         Vector3 touchPos = new Vector3();
	         touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
	         camera.unproject(touchPos);
	         shipR.x = touchPos.x - 64 / 2;
	      }
	      if(Gdx.input.isKeyPressed(Keys.LEFT)) shipR.x -= 600 * Gdx.graphics.getDeltaTime();
	      if(Gdx.input.isKeyPressed(Keys.RIGHT)) shipR.x += 600 * Gdx.graphics.getDeltaTime();

	      // make sure the bucket stays within the screen bounds
	      if(shipR.x < 0) shipR.x = 0;
	      if(shipR.x > 770 - 64) shipR.x = 770 - 64;

	      // check if we need to create a new raindrop
	      if(TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();

	      // move the raindrops, remove any that are beneath the bottom edge of
	      // the screen or that hit the bucket. In the latter case we play back
	      // a sound effect as well.
	      for (Iterator<Rectangle> iter = raindrops.iterator(); iter.hasNext(); ) {
	         Rectangle raindrop = iter.next();
	         raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
	         if(raindrop.y + 64 < 0) iter.remove();
	         if(raindrop.overlaps(shipR)) {
	            //dropSound.play();
	            iter.remove();
	         }
	      }
	}
	
	@Override
	public void dispose () {
	      // dispose of all the native resources
		enemy1.dispose();
		bucketImage.dispose();
		//dropSound.dispose();
		//rainMusic.dispose();
		batch.dispose();
	}
}
