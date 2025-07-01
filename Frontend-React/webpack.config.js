const path = require('path');
const fs = require('fs');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const webpack = require('webpack');

require('dotenv').config({ path: './.env' }); 

module.exports = {
  mode: 'development',
  entry: {
    bundle: path.resolve(__dirname, 'src/index.js'),
  },
  output: {
    path: path.resolve(__dirname, 'dist'),
    filename: '[name].[contenthash].js',
    clean: true,
    assetModuleFilename: '[name][ext]',
  },
  devtool: 'source-map',
  devServer: {
    static: {
      directory: path.resolve(__dirname, 'dist/'),
    },
    server: {
      type: 'https',
      options: {
        key: fs.readFileSync(path.resolve(__dirname, 'ssl/key.pem')),
        cert: fs.readFileSync(path.resolve(__dirname, 'ssl/cert.pem')),
      },
    },
    port: 3000,
    compress: true,
    open: true,
    hot: true,
    historyApiFallback: true,
    proxy: [
      {
        context: ['/api'],
        target: 'https://localhost:8443',
        secure: false,
        changeOrigin: true,
      },
    ],
  },
  module: {
    rules: [
      {
        test: /\.css$/,
        use: [ 'style-loader', 'css-loader', 'postcss-loader' ],
      },
      {
        test: /\.(js|jsx)$/,
        exclude: /node_modules/,
        use: {
          loader: 'babel-loader',
        },
      },
      {
        test: /\.(png|svg|jpg|jpeg|gif)$/i,
        type: 'asset/resource',
      }
    ],
  },
  plugins: [
    new HtmlWebpackPlugin({
      title: 'Marine Tracker',
      filename: 'index.html',
      template: 'src/template.html',
    }),
    new webpack.DefinePlugin({
      "process.env": JSON.stringify(process.env),
    }),
  ],
};
