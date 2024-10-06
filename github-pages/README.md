


<h1 align="center">Mobile App Landing for GitHub Pages</h1>

## Made for [NeonFrame.app](https://neonframe.app)

### Based on the original [template created by Sandoche](https://github.com/sandoche/Mobile-app-landingpage-template) and adapted to use with GitHub Actions:
```
.github/workflows/jekyll.yml
```

### [Article how to easily make the landing with GitHub Pages](https://www.linkedin.com/pulse/beautiful-landing-mobile-app-github-pages-dmitri-chernysh-tfakf/)
##

## 💡 Features
Mobile App Landing Page Template comes with pre-installed features and options:
- Display app icon
- Show unlimited app screenshots
- Link to Google Play
- Link to the AppStore
- Link to the Web App
- Press mention section
- Product Hunt floating prompt
- Privacy policy Page
- Google Analytics
- Cookie Consent
- Automatic dark theme
- Doorbell widget
- Github forking banner

## ✨ Demo
Check out websites using the Mobile App Template:

- https://neonframe.app
- https://closetalk.app
- https://mobileapplandingpage.learn.uno (demo website)


## 📖 How to use

### The normal way

1. Fork this project
2. Edit `_config.yml`, feel free to commut/uncomment what you need (google analtytics, or github for example)
3. Edit `_data/app.yml` with your app data
4. Update the text from `_data/strings.yml`, you can customize there the footer's links
5. Edit icons and screenshots inside the `_images` folder and `icon.png` in the root
6. Edit `_src/index.js` to update the product hunt modal (or to remove it) and to remove the darkmode plugin if you don't want it
7. Deploy (on netlify, gitpages or surge, they are all free)

### The no-code way

1. Go to https://t3mpl.n4no.com/editor/#manifest=../templates/mobile-app-landing-page/template.yaml
2. Edit the settings on the left part
3. Click on the `Publish` button then `Save Webpage as .zip`
4. Unzip and upload the folder to your server (you can drag'n'drop it in Netlify to host it there for free)

## ⚙️ How to run

### Pre-requisites
- NodeJS
- Ruby, Bundler

### Install
```
npm install
bundler install
```

### Development
```
npm start
```

### Build
```
npm run build
```

### Update gem version (Gemfile.lock)
```
   bundle install
```

### More documentation
This templates uses [Jekyll-webpack-boilerplate](https://github.com/sandoche/Jekyll-webpack-boilerplate), read more documentation there.

