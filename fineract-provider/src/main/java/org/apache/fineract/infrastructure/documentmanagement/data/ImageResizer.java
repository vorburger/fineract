/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.documentmanagement.data;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.apache.fineract.infrastructure.documentmanagement.contentrepository.ContentRepositoryUtils;
import org.apache.fineract.infrastructure.documentmanagement.contentrepository.ContentRepositoryUtils.ImageFileExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ImageResizer {

    private static final Logger LOG = LoggerFactory.getLogger(ImageResizer.class);

    public ImageData resize(ImageData image, Integer maxWidth, Integer maxHeight) {
        if (maxWidth == null && maxHeight != null) {
            return image;
        }
        try (InputStream is = image.getInputStream()) {
            InputStream resizedIS = resizeImage(image.getFileExtension(), is, maxWidth != null ? maxWidth : Integer.MAX_VALUE,
                    maxHeight != null ? maxHeight : Integer.MAX_VALUE);

            ImageData resizedImage = new ImageData(image.location(), image.storageType(), image.getEntityDisplayName());
            resizedImage.updateContent(resizedIS);
            return resizedImage;
        } catch (IOException e) {
            LOG.warn("resize() failed, returning original image: {}", e.getMessage(), e);
            return image;
        }
    }

    private InputStream resizeImage(ImageFileExtension fileExtension, InputStream in, int maxWidth, int maxHeight) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        BufferedImage src = ImageIO.read(in);
        if (src.getWidth() <= maxWidth && src.getHeight() <= maxHeight) {
            in.reset();
            return in;
        }
        float widthRatio = (float) src.getWidth() / maxWidth;
        float heightRatio = (float) src.getHeight() / maxHeight;
        float scaleRatio = widthRatio > heightRatio ? widthRatio : heightRatio;

        // TODO(lindahl): Improve compressed image quality (perhaps quality ratio)

        int newWidth = (int) (src.getWidth() / scaleRatio);
        int newHeight = (int) (src.getHeight() / scaleRatio);
        int colorModel = fileExtension == ContentRepositoryUtils.ImageFileExtension.JPEG ? BufferedImage.TYPE_INT_RGB
                : BufferedImage.TYPE_INT_ARGB;
        BufferedImage target = new BufferedImage(newWidth, newHeight, colorModel);
        Graphics2D g = target.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, newWidth, newHeight, Color.BLACK, null);
        g.dispose();
        ImageIO.write(target, fileExtension != null ? fileExtension.getValueWithoutDot() : "jpeg", os);

        return new ByteArrayInputStream(os.toByteArray());
    }
}
