# Aula 2: Exercício resolvido


Essa aula possui a resolução do exercício da aula 2. O exercício solicitava a criação de um quadrado colorido.

A resolucão está em duas classes:

- RotatingSquare.java: Que possui apenas o quadrado girando, sem a criação das funções (exercício a) 
- RefactoredRotatingSquare: Já com as funções evitando duplicação de código (exercício b)

No exercício é criado o index buffer, através do código
 
```java 
//Criação do Index Buffer
int indexData[] = new int[] {
        0, 2, 3,   //Vertices do primeiro triangulo
        0, 3, 1    //Segundo triangulo
};

indices = glGenBuffers();
glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indices);
glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexData, GL_STATIC_DRAW);
glBindVertexArray(0);
```

Note que isso também muda a rotina de desenho. Agora, no lugar da função `glDrawArrays` a função `glDrawElements` deverá 
ser chamada, juntamente com o bind do index buffer:

```java
glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indices);
glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
```