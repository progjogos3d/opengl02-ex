package br.pucpr.cg;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import br.pucpr.mage.Keyboard;
import br.pucpr.mage.Scene;
import br.pucpr.mage.Shader;
import br.pucpr.mage.Window;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

/**
 * Exercicio a) da aula 2
 */

public class RotatingSquare implements Scene {
	private Keyboard keys = Keyboard.getInstance();

	/** Esta variável guarda o identificador da malha (Vertex Array Object) do triângulo */
	private int vao;


	/** Guarda o id do buffer com todas as posições do vértice. */
	private int positions;

	/** Guarda o id do buffer com todas as cores do vértice */
	private int colors;

	/** Guarda o id do index buffer */
	private int indices;

	/** Guarda o id do shader program, após compilado e linkado */
	private int shader;

	/** Angulo que o triangulo está */
	private float angle;

	@Override
	public void init() {
		//Define a cor de limpeza da tela
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		//------------------
		//Criação da malha
		//------------------

		//O processo de criação da malha envolve criar um Vertex Array Object e associar a ele um buffer, com as
		// posições dos vértices do triangulo.

		//Criação do Vertex Array Object (VAO)
		vao = glGenVertexArrays();

		//Informamos a OpenGL que iremos trabalhar com esse VAO
		glBindVertexArray(vao);


		//Criação do buffer de posições
		//------------------------------
		float[] vertexData = new float[] {
				-0.5f,  0.5f,   //Vertice 0
				 0.5f,  0.5f,   //Vertice 1
				-0.5f, -0.5f,   //Vertice 2
				 0.5f, -0.5f    //Vertice 3
		};

		//Solicitamos a criação de um buffer na OpenGL, onde esse array será guardado
		positions = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, positions);
		glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);

		//Criação do buffer de cores
		//------------------------------
		float[] colorData = new float[] {
				1.0f, 0.0f, 0.0f, //Vertice 0
				1.0f, 1.0f, 1.0f, //Vertice 1
				0.0f, 1.0f, 0.0f, //Vertice 2
				0.0f, 0.0f, 1.0f, //Vertice 3
		};
		colors = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, colors);
		glBufferData(GL_ARRAY_BUFFER, colorData, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		//Criação do Index Buffer
		int indexData[] = new int[] {
				0, 2, 3,   //Vertices do primeiro triangulo
				0, 3, 1    //Segundo triangulo
		};

		indices = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indices);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexData, GL_STATIC_DRAW);
		glBindVertexArray(0);

		//------------------------------
		//Carga/Compilação dos shaders
		//------------------------------
		shader = Shader.loadProgram("basic.vert", "basic.frag");
	}

	@Override
	public void update(float secs) {
		//Testa se a tecla ESC foi pressionada
		if (keys.isPressed(GLFW_KEY_ESCAPE)) {
			//Fecha a janela, caso tenha sido
			glfwSetWindowShouldClose(glfwGetCurrentContext(), true);
			return;
		}

		//Somamos alguns graus de modo que o angulo mude 180 graus por segundo
		angle += Math.toRadians(180) * secs;
	}

	@Override
	public void draw() {
		//Solicita a limpeza da tela
		glClear(GL_COLOR_BUFFER_BIT);

		//Precisamos dizer qual VAO iremos desenhar
		glBindVertexArray(vao);

		//E qual shader program irá ser usado durante o desenho
		glUseProgram(shader);


		//Associação da variável World ao shader
		//--------------------------------------
		//Criamos um objeto da classe FloatBuffer
		FloatBuffer transform = BufferUtils.createFloatBuffer(16);

		//Criamos uma matriz de rotação e a enviamos para o buffer transform
		new Matrix4f().rotateY(angle).get(transform);

		//Procuramos pelo id da variável uWorld, dentro do shader
		int uWorld = glGetUniformLocation(shader, "uWorld");

		// Copiamos os dados do buffer para a variável que está no shader
		glUniformMatrix4fv(uWorld, false, transform);


		//Associação do buffer positions a variável aPosition
		//---------------------------------------------------
		int aPosition = glGetAttribLocation(shader, "aPosition");
		glEnableVertexAttribArray(aPosition);
		glBindBuffer(GL_ARRAY_BUFFER, positions);
		glVertexAttribPointer(aPosition, 2, GL_FLOAT, false, 0, 0);


		//Associação do buffer cores a variável aColor
		//---------------------------------------------------
		int aColor = glGetAttribLocation(shader, "aColor");
		glEnableVertexAttribArray(aColor);
		glBindBuffer(GL_ARRAY_BUFFER, colors);
		glVertexAttribPointer(aColor, 3, GL_FLOAT, false, 0, 0);

		//Indices
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indices);

		//Comandamos a pintura com indicando que 6 índices serão desenhados
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

		//Faxina
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		glDisableVertexAttribArray(aPosition);
		glDisableVertexAttribArray(aColor);
		glBindVertexArray(0);
		glUseProgram(0);
	}

	@Override
	public void deinit() {
	}

	public static void main(String[] args) {
		new Window(new RotatingSquare()).show();
	}
}