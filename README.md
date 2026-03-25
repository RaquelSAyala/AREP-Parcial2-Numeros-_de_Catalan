# Números de Catalan – Microservicios

# Raquel Selma

Pequeño sistema de microservicios en Spring Boot para investigar los **Números de Catalan** y un esquema de **proxy con round‑robin** desplegable en AWS EC2.

Componentes:
- `math-service`: servicio REST que calcula la secuencia de Catalan.
- `proxy-service`: proxy HTTP que reparte las peticiones entre dos instancias de `math-service` usando round‑robin.
- Cliente web: página HTML5 + JS básico servida por `proxy-service`.

Tecnologías: Maven, Git/GitHub, Spring Boot, HTML5, JavaScript (sin librerías externas), AWS EC2.

---

## 1. Servicio de Catalan (`math-service`)

**Endpoint**
- Método: `GET`
- URL: `/catalan`
- Parámetro de query: `value` (entero, $n \ge 0$)

Ejemplo (local):

```bash
curl "http://localhost:8080/catalan?value=10"
```

Respuesta JSON:

```json
{
	"operation": "Secuencia de Catalan",
	"input": 10,
	"output": "1, 1, 2, 5, 14, 42, 132, 429, 1430, 4862, 16796"
}
```

**Implementación**
- Se usa la recurrencia:
	- $C_0 = 1$
	- Para $n \ge 1$: $C_n = \sum_{i=0}^{n-1} C_i \cdot C_{n-1-i}$
- Programación dinámica con un arreglo de `BigInteger` para calcular $C_0,\dots,C_n$ a partir de los previos.
- No se usan combinatorias ni funciones de librería para Catalan.

Código en `math-service/src/main/java/com/eci/MathController.java`.

---

## 2. Proxy con round‑robin (`proxy-service`)

**Endpoint del proxy**
- Método: `GET`
- URL: `/catalan`
- Parámetro de query: `value` (entero, $n \ge 0$)

El proxy recibe la petición del cliente y la reenvía a una de las instancias de `math-service` usando un contador round‑robin. Si una instancia falla, intenta con la otra.

**Configuración por variables de entorno** (en la máquina del proxy):

```bash
export MATH_SERVICE_URL_1=http://<IP_PRIVADA_MATH_1>:8080
export MATH_SERVICE_URL_2=http://<IP_PRIVADA_MATH_2>:8080
```

El proxy construye internamente URLs como:

```text
<MATH_SERVICE_URL_X>/catalan?value=10
```

Lógica en `proxy-service/src/main/java/com/eci/ProxyController.java` usando `HttpURLConnection` (como en el ejemplo dado en el enunciado).

---

## 3. Cliente web (HTML5 + JS)

Archivo: `proxy-service/src/main/resources/static/index.html`.

- Formulario HTML con un campo para `value` (entero no negativo).
- Código JavaScript básico con `XMLHttpRequest` que hace una llamada asíncrona a:
	- `GET /catalan?value=n` del **proxy**.
- El cliente **no calcula** la secuencia; solo muestra el JSON que devuelve el proxy.

Al levantar `proxy-service`, se puede abrir en el navegador:

```text
http://<IP_O_DOMINIO_PROXY>:8081/
```

---

## 4. Ejecución local

Requisitos: Java 17 (Amazon Corretto recomendado) y Maven.

Para compilar ambos servicios desde la raíz del proyecto:

```bash
cd math-service
mvn clean package

cd ../proxy-service
mvn clean package
```

```bash
git clone https://github.com/<TU_USUARIO>/<TU_REPO>.git
cd <TU_REPO>

# Terminal 1: math-service
cd math-service
mvn spring-boot:run

# Terminal 2: proxy-service
cd ../proxy-service
export MATH_SERVICE_URL_1=http://localhost:8080
export MATH_SERVICE_URL_2=http://localhost:8080
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

Probar desde el navegador:

```text
http://localhost:8081/
```

---

## 5. Despliegue en AWS EC2 (resumen)

1. Crear **tres** instancias EC2 (Amazon Linux 2):
	 - EC2 A y B: `math-service`.
	 - EC2 C: `proxy-service` + cliente web.
2. Instalar Java 17 y Maven en cada instancia (ver guía de Corretto 17 de AWS).
3. En EC2 A y B:

	 ```bash
	 git clone https://github.com/<TU_USUARIO>/<TU_REPO>.git
	 cd <TU_REPO>/math-service
	 mvn package
	 java -jar target/math-service-0.0.1-SNAPSHOT.jar
	 ```

4. En EC2 C (proxy):

	 ```bash
	 git clone https://github.com/<TU_USUARIO>/<TU_REPO>.git
	 cd <TU_REPO>/proxy-service
	 mvn package

	 export MATH_SERVICE_URL_1=http://<IP_PRIVADA_MATH_1>:8080
	 export MATH_SERVICE_URL_2=http://<IP_PRIVADA_MATH_2>:8080
	 java -jar target/proxy-service-0.0.1-SNAPSHOT.jar --server.port=8081
	 ```

5. Abrir en el navegador la IP pública de la instancia del proxy:

```text
http://<IP_PUBLICA_PROXY>:8081/
```

Con esto se cumple:
- Cálculo de Números de Catalan con programación dinámica y `BigInteger`.
- Servicio REST `GET /catalan?value=n`.
- Despliegue en al menos dos instancias EC2 para el servicio numérico.
- Proxy en otra instancia EC2 con algoritmo round‑robin configurable por variables de entorno.
- Cliente web HTML5 + JS que invoca el proxy de forma asíncrona.