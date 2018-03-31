/*******************************************************************************
** Entropy.java
** Part of the Java Mutual Information toolbox
** 
** Author: Adam Pocock
** Created: 20/1/2012
**
**  Copyright 2012 Adam Pocock, The University Of Manchester
**  www.cs.manchester.ac.uk
**
**  This file is part of MIToolboxJava.
**
**  MIToolboxJava is free software: you can redistribute it and/or modify
**  it under the terms of the GNU Lesser General Public License as published by
**  the Free Software Foundation, either version 3 of the License, or
**  (at your option) any later version.
**
**  MIToolboxJava is distributed in the hope that it will be useful,
**  but WITHOUT ANY WARRANTY; without even the implied warranty of
**  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
**  GNU Lesser General Public License for more details.
**
**  You should have received a copy of the GNU Lesser General Public License
**  along with MIToolboxJava.  If not, see <http://www.gnu.org/licenses/>.
**
*******************************************************************************/

package featureSelectionMetricsPackage;

/**
 * Implements common discrete Shannon Entropy functions.
 * Provides: univariate entropy H(X),
 *           conditional entropy H(X|Y),
 *           joint entropy H(X,Y).
 * Defaults to log_2, and so the entropy is calculated in bits.
 * @author apocock
 */
public abstract class Entropy
{
  public static double LOG_BASE = 2.0;

  private Entropy() {}

  /**
   * Calculates the univariate entropy H(X) from a vector.
   * Uses histograms to estimate the probability distributions, and thus the entropy.
   * The entropy is bounded 0 &#8804; H(X) &#8804; log |X|, where log |X| is the log of the number
   * of states in the random variable X. 
   *
   * @param  dataVector  Input vector (X). It is discretised to the floor of each value before calculation.
   * @return The entropy H(X).
   */
  public static double calculateEntropy(double[] dataVector)
  {
    ProbabilityState state = new ProbabilityState(dataVector);

    double entropy = 0.0;
    for (Double prob : state.probMap.values())
    {
      if (prob > 0) 
      {
        entropy -= prob * Math.log(prob);
      }
    }

    entropy /= Math.log(LOG_BASE);
    
    return entropy;
  }//calculateEntropy(double [])

  
}//class Entropy
