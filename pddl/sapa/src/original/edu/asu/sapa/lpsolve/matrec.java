/* $Header: /var/lib/cvsroot/Sapa2/src/edu/asu/sapa/lpsolve/matrec.java,v 1.1 2004/06/14 09:36:10 bentonj Exp $ */
/* $Log: matrec.java,v $
/* Revision 1.1  2004/06/14 09:36:10  bentonj
/* added LP stuff for post-processing.  fixed a major bug in search (wrong event times associated with events on rare occasions (recharge in rover domain caused this a lot)).
/* also, separation of static predicates/functions now fully functional... as far as we can tell. :)
/*
/* Revision 1.1  2004/05/11 23:15:58  minh
/* test comments.....
/* I did nothing .. hehehe...
/*
# Revision 1.2  1996/06/06  19:47:20  hma
# added package statement
#
# Revision 1.1  1996/05/21  02:04:15  hma
# Initial revision
# */

package edu.asu.sapa.lpsolve;

public class matrec {
	int row_nr;
	double value;

	public matrec(int r, double v) {
		row_nr = r;
		value = v;
	}
}
