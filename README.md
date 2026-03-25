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
```
<img width="1502" height="145" alt="Screenshot 2026-03-25 151100" src="https://github.com/user-attachments/assets/30bf5a48-173b-4b31-8f2b-321fb78f5687" />

```bash
cd ../proxy-service
mvn clean package
```
<img width="1474" height="556" alt="Screenshot 2026-03-25 151632" src="https://github.com/user-attachments/assets/81c0eaed-34bc-41b3-a7da-792ce8f22b73" />


```bash
# Terminal 1: math-service
cd math-service
mvn spring-boot:run
```
<img width="1505" height="715" alt="Screenshot 2026-03-25 151840" src="https://github.com/user-attachments/assets/9ffe606d-26e1-452b-84c1-d09bf828c612" />

```bash
# Terminal 2: proxy-service
cd ../proxy-service
$env:MATH_SERVICE_URL_1="http://localhost:8080"
$env:MATH_SERVICE_URL_2="http://localhost:8080"
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```
<img width="1164" height="49" alt="Screenshot 2026-03-25 152002" src="https://github.com/user-attachments/assets/5a7d12ad-53bc-4a94-b16b-6d62c42abc7e" />

<img width="1486" height="735" alt="Screenshot 2026-03-25 152028" src="https://github.com/user-attachments/assets/6aa869cd-dbbc-4865-a7ab-c1488f4ae5e6" />


Probar desde el navegador:

```text
http://localhost:8081/
```

<img width="1919" height="324" alt="Screenshot 2026-03-25 152059" src="https://github.com/user-attachments/assets/5ffe0c86-4aaf-424c-985e-ade6c54245cc" />


## AWS Despliegue


<img width="1663" height="118" alt="image" src="https://github.com/user-attachments/assets/f0e4472c-2244-4392-be99-30fed45597e9" />

### Proxy

<img width="1686" height="774" alt="Screenshot 2026-03-25 153813" src="https://github.com/user-attachments/assets/c6c5e77e-3258-4eb6-9fb5-68f2a2d5e7c2" />

## mathService1
<img width="1639" height="771" alt="image" src="https://github.com/user-attachments/assets/3475b84b-49d5-4fa7-9c98-df728f6de5a8" />

## mathService2
<img width="1665" height="777" alt="image" src="https://github.com/user-attachments/assets/ad3b4882-1b2b-42d1-afea-4f6aace46675" />

## conexión 

<img width="1913" height="1035" alt="image" src="https://github.com/user-attachments/assets/25ab079c-ad9f-4b2d-bd4d-1689221b3dc2" />

## Verificación 
```http://ec2-44-211-124-58.compute-1.amazonaws.com:8080/ ``` o ```http://ec2-54-236-113-75.compute-1.amazonaws.com:8080/```

<img width="1919" height="418" alt="image" src="https://github.com/user-attachments/assets/b14ec0bc-e062-49ef-916c-0bac612a9e72" />



