package br.pucpr.cg;

import br.pucpr.mage.Keyboard;
import br.pucpr.mage.Scene;
import br.pucpr.mage.Shader;
import br.pucpr.mage.Window;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * Exercicio b) da aula 2
 *
 * Melhoria do código da classe RotatingSquare criando as funções createBuffer, createIndexBuffer e setAttribute.
 */
public class RefactoredRotatingSquare implements Scene {
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

	/**
	 * Cria um ARRAY_BUFFER com os floats passados por parâmetro
	 * @param data Os dados para incluirmos no buffer
	 * @return O id do buffer criado
	 */
	private int createBuffer(float ... data) {
		int id = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, id);
		glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
		return id;
	}
	/**
	 * Cria um ELEMENT_ARRAY_BUFFER com os floats passados por parâmetro
	 * @param indices Os índices
	 * @return O id do buffer criado
	 */
	private int createIndexBuffer(int ... indexData) {
		int id = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexData, GL_STATIC_DRAW);
		glBindVertexArray(0);
		return id;
	}

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
		positions = createBuffer(
			-0.5f,  0.5f,   //Vertice 0
			 0.5f,  0.5f,   //Vertice 1
			-0.5f, -0.5f,   //Vertice 2
			 0.5f, -0.5f    //Vertice 3
		);

		//Criação do buffer de cores
		//------------------------------
		colors = createBuffer(
			1.0f, 0.0f, 0.0f, //Vertice 0
			1.0f, 1.0f, 1.0f, //Vertice 1
			0.0f, 1.0f, 0.0f, //Vertice 2
			0.0f, 0.0f, 1.0f  //Vertice 3
		);

		indices = createIndexBuffer(
			0, 2, 3,   //Vertices do primeiro triangulo
			0, 3, 1    //Segundo triangulo
		);

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

	/**
	 * Associa uma variável de atributo do shader a um buffer
	 * @param attribute Nome do atributo
	 * @param buffer Buffer a ser associado
	 * @param size Tamanho do elemento do buffer
	 * @return O id do atributo habilitado
	 */
	public int setAttribute(String attribute, int buffer, int size) {
		int id = glGetAttribLocation(shader, attribute);
		glEnableVertexAttribArray(id);
		glBindBuffer(GL_ARRAY_BUFFER, buffer);
		glVertexAttribPointer(id, size, GL_FLOAT, false, 0, 0);
		return id;
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
		new Matrix4f().rotateY(angle).get(transform);
		int uWorld = glGetUniformLocation(shader, "uWorld");
		glUniformMatrix4fv(uWorld, false, transform);


		//Associação do buffer positions a variável aPosition
		//---------------------------------------------------
		int aPosition = setAttribute("aPosition", positions, 2);

		//Associação do buffer cores a variável aColor
		//---------------------------------------------------
		int aColor = setAttribute("aColor", colors, 3);

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
		new Window(new RefactoredRotatingSquare()).show();
	}
}