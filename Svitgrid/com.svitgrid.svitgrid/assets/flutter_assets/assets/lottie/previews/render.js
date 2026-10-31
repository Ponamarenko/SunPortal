const puppeteer = require('puppeteer-core');
const fs = require('fs');
const path = require('path');

const LOTTIE_DIR = path.resolve(__dirname, '..');
const OUT_DIR = __dirname;

const files = [
  // Day versions (existing)
  { file: 'home_sun.json', label: 'home_sun_day' },
  { file: 'home_rain.json', label: 'home_rain_day' },
  { file: 'home_snow.json', label: 'home_snow_day' },
  { file: 'home_windy.json', label: 'home_windy_day' },
  // Night versions
  { file: 'home_night.json', label: 'home_night_clear' },
  { file: 'home_rain_night.json', label: 'home_rain_night' },
  { file: 'home_snow_night.json', label: 'home_snow_night' },
  { file: 'home_windy_night.json', label: 'home_windy_night' },
];

(async () => {
  const browser = await puppeteer.launch({
    headless: true,
    executablePath: '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
  });

  for (const { file, label } of files) {
    const jsonPath = path.join(LOTTIE_DIR, file);
    if (!fs.existsSync(jsonPath)) {
      console.log(`SKIP ${file} — not found`);
      continue;
    }
    const animData = fs.readFileSync(jsonPath, 'utf8');

    const page = await browser.newPage();
    await page.setViewport({ width: 300, height: 300 });
    await page.setContent(`
      <!DOCTYPE html>
      <html>
      <head>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/lottie-web/5.12.2/lottie.min.js"></script>
        <style>
          body { margin: 0; display: flex; align-items: center; justify-content: center; width: 300px; height: 300px; background: #f8f8f8; }
          #anim { width: 250px; height: 250px; }
        </style>
      </head>
      <body>
        <div id="anim"></div>
        <script>
          const anim = lottie.loadAnimation({
            container: document.getElementById('anim'),
            renderer: 'svg',
            loop: false,
            autoplay: false,
            animationData: ${animData}
          });
          anim.addEventListener('DOMLoaded', () => {
            // Go to ~30% through the animation to capture a representative frame
            anim.goToAndStop(Math.floor(anim.totalFrames * 0.3), true);
            window.__ready = true;
          });
        </script>
      </body>
      </html>
    `);

    await page.waitForFunction('window.__ready === true', { timeout: 10000 });
    await page.screenshot({ path: path.join(OUT_DIR, `${label}.png`) });
    console.log(`Rendered ${label}.png`);
    await page.close();
  }

  await browser.close();
  console.log('Done!');
})();
