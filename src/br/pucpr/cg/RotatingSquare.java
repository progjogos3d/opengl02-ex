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
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

/**
 * Exercicio a) da aula 2
 *
 * Alteração do código final da aula para desenhar um quadrado colorido. Utiliza index buffer para evitar a duplicação
 * de vértices.
 */
public class RotatingSquare implements Scene {
	private Keyboard keys = Keyboard.getInstance();

	/** Esta variável guarda o identificador da malha (Vertex Array Object) do triângulo */
	private int vao;

	/** Guarda o id do shader program, após compilado e linkado */
	private int shader;

	/** Angulo que o triangulo está */
	private float angle;

	@Override
	public void init() {
		//---------------------------------
		//Configurações iniciais
		//---------------------------------

		//Habilita o teste de profundidade
		glEnable(GL_DEPTH_TEST);

		//Impede o desenho quando os triângulos estiverem de costas
		glEnable(GL_CULL_FACE);

		//Descomente para desenhar só as bordas do triângulo
		//glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

		//Define a cor de limpeza da tela
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		//------------------------------
		//Carga/Compilação dos shaders
		//------------------------------
		shader = Shader.loadProgram("basic.vert", "basic.frag");

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
		var vertexData = new float[] {
			-0.5f,  0.5f,   //Vertice 0
			 0.5f,  0.5f,   //Vertice 1
			-0.5f, -0.5f,   //Vertice 2
			 0.5f, -0.5f    //Vertice 3
		};

		//Solicitamos a criação de um buffer na OpenGL, onde esse array será guardado
		var positions = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, positions);
		glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);

		//Associação do buffer positions a variável aPosition
		//---------------------------------------------------
		var aPosition = glGetAttribLocation(shader, "aPosition");
		glVertexAttribPointer(aPosition, 2, GL_FLOAT, false, 0, 0);
		glEnableVertexAttribArray(aPosition);

		//Criação do buffer de cores
		//------------------------------
		var colorData = new float[] {
			1.0f, 0.0f, 0.0f, //Vertice 0
			1.0f, 1.0f, 1.0f, //Vertice 1
			0.0f, 1.0f, 0.0f, //Vertice 2
			0.0f, 0.0f, 1.0f, //Vertice 3
		};
		var colors = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, colors);
		glBufferData(GL_ARRAY_BUFFER, colorData, GL_STATIC_DRAW);

		//Associação do buffer cores a variável aColor
		//---------------------------------------------------
		var aColor = glGetAttribLocation(shader, "aColor");
		glVertexAttribPointer(aColor, 3, GL_FLOAT, false, 0, 0);
		glEnableVertexAttribArray(aColor);

		//Criação do Index Buffer
		var indexData = new int[] {
			0, 2, 3,   //Vertices do primeiro triangulo
			0, 3, 1    //Segundo triangulo
		};

		var indices = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indices);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexData, GL_STATIC_DRAW);

		//Faxina
		glBindVertexArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
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
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		//Precisamos dizer qual VAO iremos desenhar
		glBindVertexArray(vao);

		//E qual shader program irá ser usado durante o desenho
		glUseProgram(shader);


		//Associação da variável World ao shader
		//--------------------------------------
		//Criamos um objeto da classe FloatBuffer
		try (var stack = MemoryStack.stackPush()) {
			//Criamos uma matriz de rotação e a enviamos para o buffer transform
			var transform = new Matrix4f()
					.rotateY(angle)
					.get(stack.mallocFloat(16));

			//Procuramos pelo id da variável uWorld, dentro do shader
			var uWorld = glGetUniformLocation(shader, "uWorld");

			// Copiamos os dados do buffer para a variável que está no shader
			glUniformMatrix4fv(uWorld, false, transform);
		}

		//Comandamos a pintura com indicando que 6 índices serão desenhados
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
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