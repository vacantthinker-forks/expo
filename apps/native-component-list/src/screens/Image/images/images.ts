const images = {
  require_jpg1: require('../../../../assets/images/example1.jpg'),
  require_jpg2: require('../../../../assets/images/example2.jpg'),
  require_jpg3: require('../../../../assets/images/example3.jpg'),
  require_png: require('../../../../assets/images/chapeau.png'),
  require_webp: require('../../../../assets/images/example3.webp'),
  require_webp_anim: require('../../../../assets/videos/ace.webp'),
  uri_random_unsplash: {
    uri: `https://source.unsplash.com/random?${Math.floor(Math.random() * 1000)}`,
  },
  uri_png: { uri: 'https://docs.expo.io/static/images/header-logo.png' },
  uri_jpg: { uri: 'https://docs.expo.io/static/images/flappy_00.jpg' },
  uri_gif: { uri: 'https://docs.expo.io/static/images/flappy_03.gif' },
  uri_ico: { uri: 'https://docs.expo.io/static/images/favicon.ico' },
};

export default images;
