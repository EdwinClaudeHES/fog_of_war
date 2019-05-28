
package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthoCachedTiledMapRenderer;

public class MyGdxGame extends ApplicationAdapter
	{

	SpriteBatch batch;
	Texture background;
	Texture mask;
	Texture forground;
	ShaderProgram shader;
	OrthographicCamera cam;
	float ppm = 0.6f;

	int width;
	int height;

	OrthoCachedTiledMapRenderer tiledMapRenderer;

	final String VERTEX = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" +

			"uniform mat4 u_projTrans;\n" + " \n" + "varying vec4 vColor;\n" + "varying vec2 vTexCoord;\n" +

			"void main() {\n" + "	vColor = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" + "	vTexCoord = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" + "	gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" + "}";

	final String FRAGMENT =
			//GL ES specific stuff
			"#ifdef GL_ES\n" //
					+ "#define LOWP lowp\n" //
					+ "precision mediump float;\n" //
					+ "#else\n" //
					+ "#define LOWP \n" //
					+ "#endif\n" + //
					"varying LOWP vec4 vColor;\n" + "varying vec2 vTexCoord;\n" + "uniform sampler2D u_texture;\n" + "uniform sampler2D u_texture1;\n" + "uniform sampler2D u_mask;\n" + "void main(void) {\n" + "	//sample the colour from the first texture\n"
					+ "	vec4 texColor0 = texture2D(u_texture, vTexCoord);\n" + "\n" + "	//sample the colour from the second texture\n" + "	vec4 texColor1 = texture2D(u_texture1, vTexCoord);\n" + "\n" + "	//get the mask; we will only use the alpha channel\n"
					+ "	float mask = texture2D(u_mask, vTexCoord).a;\n" + "\n" + "	//interpolate the colours based on the mask\n" + "	gl_FragColor = vColor * mix(texColor0, texColor1, mask);\n" + "}";

	@Override
	public void create()
		{

		width = (int)(Gdx.graphics.getWidth() / ppm);
		height = (int)(Gdx.graphics.getHeight() / ppm);

		FrameBuffer fb = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);

		TiledMap map = new TmxMapLoader().load("map.tmx");
		tiledMapRenderer = new OrthoCachedTiledMapRenderer(map);

		fb.begin();

		tiledMapRenderer.render();

		fb.end();

		background = fb.getColorBufferTexture();

		ShaderProgram.pedantic = false;

		batch = new SpriteBatch();

		shader = new ShaderProgram(VERTEX, FRAGMENT);

		cam = new OrthographicCamera(width, height);

		//		background = new Texture("dirt.jpg");
		mask = new Texture("mask2.png");
		forground = new Texture("grass.png");

		//Good idea to log any warnings if they exist
		if (shader.getLog().length() != 0)
			{
			System.out.println(shader.getLog());
			}

		shader.begin();
		shader.setUniformi("u_texture1", 1);
		shader.setUniformi("u_mask", 2);
		shader.end();

		//bind mask to glActiveTexture(GL_TEXTURE2)
		mask.bind(2);

		//bind dirt to glActiveTexture(GL_TEXTURE1)
		background.bind(1);

		//		tiledMapRenderer.getSpriteCache().setShader(shader);

		//now we need to reset glActiveTexture to zero!!!! since sprite batch does not do this for us
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

		//tex0 will be bound when we call SpriteBatch.draw

		batch = new SpriteBatch(1000, shader);
		batch.setShader(shader);

		batch.setProjectionMatrix(cam.combined);

		tiledMapRenderer.setView(cam);

		//cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		//cam.setToOrtho(false);

		}

	@Override
	public void render()
		{
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		tiledMapRenderer.render();

		batch.begin();

		batch.draw(forground, 0, 0);

		batch.end();
		}

	@Override
	public void dispose()
		{
		batch.dispose();
		//		img.dispose();
		}
	}
