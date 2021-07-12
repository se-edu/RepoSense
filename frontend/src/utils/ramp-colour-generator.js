import seedrandom from 'seedrandom';

const randomGenerator = seedrandom('Seeded Random Generator');

function getRandomHex() {
  const maxHexColorValue = 16777214;
  // excludes #000000 and #FFFFFF as they are reserved
  return `#${Math.round(randomGenerator() * maxHexColorValue + 1).toString(16).padStart(6, '0')}`;
}

function rgb2lab(rgb) {
  let r = rgb[0] / 255;
  let g = rgb[1] / 255;
  let b = rgb[2] / 255;
  let x;
  let y;
  let z;
  r = (r > 0.04045) ? ((r + 0.055) / 1.055) ** 2.4 : r / 12.92;
  g = (g > 0.04045) ? ((g + 0.055) / 1.055) ** 2.4 : g / 12.92;
  b = (b > 0.04045) ? ((b + 0.055) / 1.055) ** 2.4 : b / 12.92;
  x = (r * 0.4124 + g * 0.3576 + b * 0.1805) / 0.95047;
  y = (r * 0.2126 + g * 0.7152 + b * 0.0722) / 1.00000;
  z = (r * 0.0193 + g * 0.1192 + b * 0.9505) / 1.08883;
  x = (x > 0.008856) ? (x ** (1 / 3)) : (7.787 * x) + 16 / 116;
  y = (y > 0.008856) ? (y ** (1 / 3)) : (7.787 * y) + 16 / 116;
  z = (z > 0.008856) ? (z ** (1 / 3)) : (7.787 * z) + 16 / 116;
  return [(116 * y) - 16, 500 * (x - y), 200 * (y - z)];
}

// this delta E (perceptual color distance) implementation taken from @antimatter15 from
// github: https://github.com/antimatter15/rgb-lab
function deltaE(rgbA, rgbB) {
  const labA = rgb2lab(rgbA);
  const labB = rgb2lab(rgbB);
  const deltaL = labA[0] - labB[0];
  const deltaA = labA[1] - labB[1];
  const deltaB = labA[2] - labB[2];
  const c1 = Math.sqrt(labA[1] * labA[1] + labA[2] * labA[2]);
  const c2 = Math.sqrt(labB[1] * labB[1] + labB[2] * labB[2]);
  const deltaC = c1 - c2;
  let deltaH = deltaA * deltaA + deltaB * deltaB - deltaC * deltaC;
  deltaH = deltaH < 0 ? 0 : Math.sqrt(deltaH);
  const sc = 1.0 + 0.045 * c1;
  const sh = 1.0 + 0.015 * c1;
  const deltaLKLSL = deltaL / (1.0);
  const deltaCKCSC = deltaC / (sc);
  const deltaHKSHS = deltaH / (sh);
  const distance = deltaLKLSL * deltaLKLSL + deltaCKCSC * deltaCKCSC + deltaHKSHS * deltaHKSHS;
  return distance < 0 ? 0 : Math.sqrt(distance);
}

function hasSimilarExistingColors(existingColors, newHex) {
  const deltaEThreshold = 11;
  // the lower limit of delta E to be similar, more info at http://zschuessler.github.io/DeltaE/learn/
  return existingColors.some((existingHex) => {
    const existingRGB = window.getHexToRGB(existingHex);
    const newRGB = window.getHexToRGB(newHex);
    return deltaE(existingRGB, newRGB) < deltaEThreshold;
  });
}

function getNonRepeatingColor(existingColors) {
  let generatedHex = getRandomHex();
  while (hasSimilarExistingColors(existingColors, generatedHex)) {
    generatedHex = getRandomHex();
  }
  return generatedHex;
}

export default getNonRepeatingColor;
