# Ejercicio 3 - Mapa de Farmacias de Zaragoza

Este ejercicio en Java Android Studio es una aplicación que implementa la funcionalidad de mapas con Google Maps, integración con Firebase y uso de APIs públicas. A continuación, se describen en detalle las partes principales del código.

---

## **PantallaPrincipalActivity**

### **Descripción General**
Esta actividad actúa como punto central de la aplicación. Incluye un menú lateral de navegación (`NavigationView`) y gestiona la autenticación del usuario mediante Firebase. También proporciona un botón de cierre de sesión.

### **Elementos Destacados**
- **DrawerLayout y NavigationView**: Facilitan la navegación lateral dentro de la aplicación.
- **FirebaseHandler**: Gestiona la comunicación con Firebase Firestore para obtener datos del usuario autenticado.
- **Mapa de Farmacias**: Desde el menú lateral, el usuario puede acceder a la actividad que muestra el mapa de farmacias.

### **Funciones Clave**
- `setupNavigationView()`: Configura las acciones del menú lateral. Incluye la funcionalidad de redirigir a `MapaFarmaciasActivity` para visualizar las farmacias en un mapa.
- **Botón de Logout**: Cierra la sesión del usuario en Firebase y redirige a `LoginActivity`.

---

## **RegisterActivity**

### **Descripción General**
Permite a los usuarios registrarse en la aplicación. Incluye un formulario de registro y funcionalidades para obtener la ubicación actual del usuario al momento del registro.

### **Elementos Destacados**
- **FusedLocationProviderClient**: Se utiliza para obtener la ubicación del usuario.
- **Firebase Auth y Firestore**: Se registran las credenciales y los datos del usuario en Firebase.
- **Validación de Ubicación**: Comprueba la validez de las coordenadas antes de completar el registro.

### **Funciones Clave**
- `checkLocationAndRegister()`: Verifica permisos de ubicación y obtiene la ubicación del usuario antes de iniciar el registro.
- `fetchUserLocation()`: Intenta obtener una ubicación válida mediante GPS o red.
- `register()`: Registra al usuario en Firebase, guardando tanto sus credenciales como su ubicación.

---

## **MapaFarmaciasActivity**

### **Descripción General**
Muestra un mapa interactivo con las farmacias de Zaragoza usando Google Maps. Los datos se obtienen de una API pública del ayuntamiento de Zaragoza y se representan como marcadores en el mapa. Además muestra la posición del usuario mediante los Sensores de Localización del dispositivo.

### **Elementos Destacados**
- **Google Maps API**: Proporciona un mapa interactivo.
- **FarmaciaHandler**: Obtiene datos de farmacias desde la API pública.
- **Filtros de Búsqueda**: Un campo de búsqueda permite filtrar las farmacias mostradas en el mapa según su nombre o dirección.

### **Funciones Clave**
- `onMapReady()`: Configura el mapa y habilita controles como el zoom y la ubicación del usuario.
- `cargarFarmaciasEnMapa()`: Obtiene datos de farmacias desde la API y las representa en el mapa como marcadores.
- `filtrarFarmacias()`: Filtra los marcadores en el mapa según el texto ingresado en la barra de búsqueda.
- `bitmapDescriptorFromVector()`: Convierte un vector XML en un ícono para los marcadores.

---

## **LoginActivity**

### **Descripción General**
Permite a los usuarios iniciar sesión con sus credenciales. Incluye la opción de redirigir al registro si el usuario no tiene cuenta.

### **Elementos Destacados**
- **FirebaseAuth**: Autentica a los usuarios.
- **FirebaseHandler**: Obtiene las credenciales del usuario almacenadas en Firestore.
- **Gestión de Sesión**: Guarda el estado del usuario autenticado en `SharedPreferences`.

---

## **FirebaseHandler**

### **Descripción General**
Es una clase de utilidad para interactuar con Firebase Firestore. Proporciona métodos para CRUD de usuarios, credenciales y otros datos.

### **Funciones Clave**
- `guardarUsuario()`, `obtenerUsuario()`: Gestionan la creación y recuperación de datos de usuario.
- `obtenerUsuarioPorCorreo()`: Busca usuarios por su correo electrónico.
- `guardarCredenciales()`, `obtenerCredenciales()`: Manejan credenciales asociadas a los usuarios.

---

## **FarmaciaHandler**

### **Descripción General**
Obtiene datos de farmacias desde una API pública (`https://www.zaragoza.es/sede/servicio/farmacia.json`). Los datos incluyen coordenadas geográficas, nombres y direcciones.

### **Funciones Clave**
- `obtenerFarmacias()`: Realiza una solicitud HTTP GET para recuperar los datos de la API. Los parsea y devuelve como una lista de objetos `Farmacia`.

## Imagenes/Información adicional de la aplicación

- Para simular la ubicación del dispositivo lo hemos situado en *Villanueva De La Cañada (Madrid)*, concretamente en la *UAX*, de esta manera el usuario que se registre aparecera como su ubicación actual el punto que hemos establecido para el dispositivo. A continuación, muestro una imagen de la ubicación establecida en nuestro simulador de Android Studio:

![image](https://github.com/user-attachments/assets/0ec0cbc0-6ef2-47c9-b6c7-4255b42b14f2)

- Imagen de las ubicaciones en el mapa de las Farmacias de Zaragoza:

![image](https://github.com/user-attachments/assets/27f2187c-1d21-4f50-b117-87b2ed9b181c)

![image](https://github.com/user-attachments/assets/dab886c3-d145-48ef-9033-874ad840cece)

- Buscador de Farmacias (Filtro de ubicación en el mapa):

![image](https://github.com/user-attachments/assets/e111bec3-1eb0-4f3a-8e4a-56b347309e93)




