# Frontend Setup for MarineTracker

## Overview

This is the initialization of the frontend-React for our project. Instead of using `create-react-app` (which has recently been deprecated), the setup was done manually using **Webpack**.

This approach gives us more control over the configuration and is better aligned with current best practices in the React ecosystem.

---

## Getting Started

### Prerequisites

- **Node.js version `22.12.0`** is required. You can manage Node versions using [nvm](https://github.com/nvm-sh/nvm) or another version manager.

### Installation

1. Clone the repository and navigate to the frontend directory:

   ```bash
   cd Frontend-React
   ```

2. Install dependencies:

   ```bash
   npm install
   ```

   > If you encounter errors, try using:

   ```bash
   npm install --legacy-peer-deps
   ```

   This may be necessary due to some dependencies not yet supporting **React 19** (which we are using), while still relying on **React 18**. These differences are minimal and non-breaking for our use case.

### Running the Project

Start the development server with:

```bash
npm run dev
```

This will enable hot-reloading, so you can start coding immediately and see changes in real time.

### Access the web page
Once React is running, you can access the web page at: https://localhost:3000/

---

## Features

- **Environment Variables:** Support for `.env` files is enabled. This setup allows easy configuration of environment-specific variables. <br> 
For example, one important .env file that should be included is:<br>
```.env
REACT_APP_BACKEND_URL=https://localhost:8443/
```
- **Custom Map Component:** Includes a map component with a free-draw feature.
