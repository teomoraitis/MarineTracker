
# React Frontend

## Overview

This is the initialization of the frontend for our project. Instead of using `create-react-app` (which has recently been deprecated), the setup was done manually using **Webpack**.

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

---

## Features

- **Environment Variables:** Support for `.env` files is enabled. While we may not need them immediately, the setup allows easy configuration of environment-specific variables in the future.
- **Custom Map Component:** Includes a map component with a free-draw feature.
